package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.*;

public class SetEquipmentChecker extends BukkitRunnable {

    private final FacelessItems plugin;
    private final SetManager setManager;
    private final PassiveEffectApplier passiveEffectApplier; // NUEVO CAMPO

    // Este mapa ahora es GLOBAL para la clase y contiene los efectos activos.
    private final Map<UUID, Set<PotionEffect>> activeSetEffects = new HashMap<>();

    public SetEquipmentChecker(FacelessItems plugin, SetManager setManager) {
        this.plugin = plugin;
        this.setManager = setManager;
        this.passiveEffectApplier = new PassiveEffectApplier(plugin); // Inicializar
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkPlayerEquipment(player);
        }
    }

// SetEquipmentChecker.java

    private void checkPlayerEquipment(Player player) {
        Map<String, Integer> setPiecesCount = new HashMap<>();
        List<BaseEffect> allPassiveEffects = new ArrayList<>();

        // 1. Recolectar ÍTEMS EQUIPADOS (Armadura + Manos)
        List<ItemStack> equippedItems = new ArrayList<>();
        equippedItems.addAll(Arrays.asList(player.getInventory().getArmorContents()));
        equippedItems.add(player.getInventory().getItemInMainHand());
        equippedItems.add(player.getInventory().getItemInOffHand());

       // plugin.getLogger().info("DEBUG CHECKER: Iniciando revisión de equipos para " + player.getName());

        for (ItemStack equippedItem : equippedItems) {

            // DEBUG A: Verificación de Slot (Solo para ítems no nulos/aire)
            if (equippedItem != null && equippedItem.getType() != Material.AIR) {
             //   plugin.getLogger().info("DEBUG CHECKER: Slot Item Type: " + equippedItem.getType().name());
            }

            CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(equippedItem);

            if (customItem != null) {

                // DEBUG B: Detección de Custom Item
             //   plugin.getLogger().info("DEBUG CHECKER: Ítem custom DETECTADO: " + customItem.getKey());

                // A. ACUMULAR PASIVOS DE ÍTEM INDIVIDUAL
                List<BaseEffect> itemPassives = customItem.getPassiveEffects();
                if (!itemPassives.isEmpty()) {
                    // DEBUG C: Confirmación de Pasivas Cargadas
               //     plugin.getLogger().info("DEBUG CHECKER: Pasivas añadidas desde el ítem: " + customItem.getKey() + " (Cantidad: " + itemPassives.size() + ")");
                    allPassiveEffects.addAll(itemPassives);
                }

                // 2. CONTAR PIEZAS DE SETS (SOLO SI SON ARMADURA)
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

        // 2. Acumular los pasivos de los sets (el resto es igual)
        for (Map.Entry<String, Integer> entry : setPiecesCount.entrySet()) {
            ArmorSet set = setManager.getArmorSet(entry.getKey());
            if (set != null) {
                ArmorSetBonus bonus = set.getBonus(entry.getValue());
                if (bonus != null) {
                    allPassiveEffects.addAll(bonus.getPassiveEffects());
                }
            }
        }

        // DEBUG D: Total final de pasivas a procesar
        //plugin.getLogger().info("DEBUG CHECKER: Total de pasivas acumuladas para Applier: " + allPassiveEffects.size());

        // 3. ¡DELEGAR LA APLICACIÓN AL APPLIER!
        passiveEffectApplier.applyEffects(player, allPassiveEffects, activeSetEffects);
    }

    // Método para limpiar el rastro del jugador al desconectarse.
    public void playerQuit(Player player) {
        activeSetEffects.remove(player.getUniqueId());
    }
}