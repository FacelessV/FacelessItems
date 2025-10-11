package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.*;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class SetEquipmentChecker extends BukkitRunnable {

    private final FacelessItems plugin;
    private final SetManager setManager;
    private final PassiveEffectApplier passiveEffectApplier;

    // Solución: Usar el tipo de Bukkit explícitamente para el mapa
    private final Map<UUID, Set<org.bukkit.potion.PotionEffect>> activeSetEffects = new HashMap<>();

    public SetEquipmentChecker(FacelessItems plugin, SetManager setManager) {
        this.plugin = plugin;
        this.setManager = setManager;
        this.passiveEffectApplier = new PassiveEffectApplier(plugin);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayerEquipment(player);
        }
    }

    private void checkPlayerEquipment(Player player) {
        Map<String, Integer> setPiecesCount = new HashMap<>();
        List<BaseEffect> allPassiveEffects = new ArrayList<>();

        EffectContext context =
                new EffectContext(player, player, null, new HashMap<>(), "PASSIVE_CHECK", plugin);

        List<ItemStack> equippedItems = new ArrayList<>();
        equippedItems.addAll(Arrays.asList(player.getInventory().getArmorContents()));
        equippedItems.add(player.getInventory().getItemInMainHand());
        equippedItems.add(player.getInventory().getItemInOffHand());

        for (ItemStack equippedItem : equippedItems) {
            CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(equippedItem);

            if (customItem != null) {
                allPassiveEffects.addAll(customItem.getPassiveEffects());

                if (equippedItem.getType().name().contains("HELMET") ||
                        equippedItem.getType().name().contains("CHESTPLATE") ||
                        equippedItem.getType().name().contains("LEGGINGS") ||
                        equippedItem.getType().name().contains("BOOTS"))
                {
                    for (ArmorSet set : setManager.getArmorSets()) {
                        if (set.containsItem(customItem.getKey())) {
                            setPiecesCount.put(set.getKey(), setPiecesCount.getOrDefault(set.getKey(), 0) + 1);
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, Integer> entry : setPiecesCount.entrySet()) {
            ArmorSet set = setManager.getArmorSet(entry.getKey());
            if (set != null) {
                ArmorSetBonus bonus = set.getBonus(entry.getValue());
                if (bonus != null) {
                    allPassiveEffects.addAll(bonus.getPassiveEffects());
                }
            }
        }

        // ==========================================================
        // 3. PROCESAMIENTO DE EFECTOS PASIVOS (Ejecución Directa y Filtrado)
        // ==========================================================

        List<BaseEffect> potionAndModifierEffects = new ArrayList<>();

        for (BaseEffect effect : allPassiveEffects) {

            boolean conditionsMet = effect.getConditions().isEmpty() ||
                    (effect.getConditions().stream().allMatch(c -> c.check(context)));

            if (conditionsMet) {

                // CASO A: Efectos que son solo Pociones o Modificadores de Daño (Pasan al Applier)
                if (effect instanceof PotionEffect ||
                        effect instanceof DamageMultiplierEffect)
                {
                    potionAndModifierEffects.add(effect);
                }

                // CASO B: Efectos de UTILIDAD que se ejecutan directamente (Traits, Stats, Growth)
                else {

                    if (effect instanceof GrowthBoostEffect growthEffect) {
                        applyGrowthBoost(player, growthEffect.getRadius(), growthEffect.getChance());
                    }
                    else if (effect instanceof AddTraitEffect ||
                            effect instanceof AddStatEffect)
                    {
                        effect.apply(context);
                    }
                }
            }
        }

        // 4. DELEGAR LA APLICACIÓN DE POCIONES AL APPLIER
        // La llamada debe ser COMPATIBLE con la nueva firma del Applier.
        passiveEffectApplier.applyEffects(player, potionAndModifierEffects, activeSetEffects);
    }

    private void applyGrowthBoost(Player player, double radius, double chance) {
        if (Math.random() > chance) {
            return;
        }

        Location center = player.getLocation();
        int r = (int) Math.ceil(radius);

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {

                    org.bukkit.block.Block block = center.clone().add(x, y, z).getBlock();

                    if (block.getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {
                        if (ageable.getAge() < ageable.getMaximumAge()) {
                            block.applyBoneMeal(org.bukkit.block.BlockFace.UP);
                        }
                    }
                }
            }
        }
    }

    public void playerQuit(Player player) {
        activeSetEffects.remove(player.getUniqueId());
    }
}