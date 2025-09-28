package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class ArmorSetChecker extends BukkitRunnable {

    private final FacelessItems plugin;
    private final SetManager setManager;

    // This map will store which potion effects a player has active from sets, to manage them properly.
    private final Map<UUID, Set<PotionEffect>> activeSetEffects = new HashMap<>();

    public ArmorSetChecker(FacelessItems plugin, SetManager setManager) {
        this.plugin = plugin;
        this.setManager = setManager;
    }

    @Override
    public void run() {
        // This code runs every second for every online player.
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayerArmor(player);
        }
    }

    private void checkPlayerArmor(Player player) {
        Map<String, Integer> setPiecesCount = new HashMap<>();
        // --- NUEVO: Lista para acumular todos los pasivos ---
        List<BaseEffect> allPassiveEffects = new ArrayList<>();

        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(armorPiece);
            if (customItem != null) {
                // Añadimos los pasivos del ítem individual
                allPassiveEffects.addAll(customItem.getPassiveEffects());

                // Contamos las piezas para los sets
                for (ArmorSet set : setManager.getArmorSets()) {
                    if (set.containsItem(customItem.getKey())) {
                        setPiecesCount.put(set.getKey(), setPiecesCount.getOrDefault(set.getKey(), 0) + 1);
                    }
                }
            }
        }

        // Acumulamos los pasivos de los sets
        for (Map.Entry<String, Integer> entry : setPiecesCount.entrySet()) {
            ArmorSet set = setManager.getArmorSet(entry.getKey());
            if (set != null) {
                ArmorSetBonus bonus = set.getBonus(entry.getValue());
                if (bonus != null) {
                    allPassiveEffects.addAll(bonus.getPassiveEffects());
                }
            }
        }

        // Ahora, procesamos TODOS los efectos pasivos acumulados
        Set<PotionEffect> effectsToApply = new HashSet<>();
        for (BaseEffect effect : allPassiveEffects) {
            if (effect instanceof bw.development.facelessItems.Effects.PotionEffect potionEffect) {
                effectsToApply.add(new PotionEffect(potionEffect.getPotionType(), 40, potionEffect.getAmplifier(), true, false));
            }
        }

        // 3. Remove old effects that are no longer active.
        Set<PotionEffect> previouslyActive = activeSetEffects.getOrDefault(player.getUniqueId(), Collections.emptySet());
        for (PotionEffect oldEffect : previouslyActive) {
            if (!effectsToApply.contains(oldEffect)) {
                player.removePotionEffect(oldEffect.getType());
            }
        }

        // 4. Apply all new/current effects.
        for (PotionEffect newEffect : effectsToApply) {
            player.addPotionEffect(newEffect);
        }

        // 5. Update the map of active effects for this player.
        activeSetEffects.put(player.getUniqueId(), effectsToApply);
    }

    // Add a method to clear a player's effects if they disconnect.
    public void playerQuit(Player player) {
        activeSetEffects.remove(player.getUniqueId());
    }
}