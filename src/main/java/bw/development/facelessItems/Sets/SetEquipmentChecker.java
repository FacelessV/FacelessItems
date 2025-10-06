package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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


        // ==========================================================
        // 3. PROCESAMIENTO DE EFECTOS DE BLOQUE (Ej: GROWTH_BOOST)
        // Estos no son pociones, por lo que no se delegan al Applier directamente.
        // ==========================================================

        for (BaseEffect effect : allPassiveEffects) {

            // --- INTEGRACIÓN DE GROWTH_BOOST ---
            if (effect instanceof bw.development.facelessItems.Effects.GrowthBoostEffect growthEffect) {

                // CRÍTICO: Chequeo de condiciones (si el talismán tiene condiciones como is_day)
                // Usamos un contexto simplificado para el chequeo de condiciones del entorno.
                bw.development.facelessItems.Effects.EffectContext context =
                        new bw.development.facelessItems.Effects.EffectContext(player, player, null, new HashMap<>(), "PASSIVE_CHECK", plugin);

                boolean conditionsMet = growthEffect.getConditions().isEmpty() ||
                        growthEffect.getConditions().stream().allMatch(c -> c.check(context));

                if (conditionsMet) {
                    // Si las condiciones se cumplen, aplicamos el tick simulado
                    applyGrowthBoost(player, growthEffect.getRadius(), growthEffect.getChance());
                }
            }
            // Aquí irían otros efectos que manipulan bloques/entidades directamente (ej: HealthStealAuraEffect)
        }

        // 4. DELEGAR LA APLICACIÓN DE POCIONES (El Applier ya sabe cómo aplicar solo pociones)
        // passiveEffectApplier.applyEffects(player, allPassiveEffects, activeSetEffects);
        // NOTA: Para evitar doble chequeo, solo pasaremos los efectos de poción restantes al Applier.

        List<BaseEffect> potionAndModifierEffects = new ArrayList<>();
        for (BaseEffect effect : allPassiveEffects) {
            if (effect instanceof bw.development.facelessItems.Effects.PotionEffect ||
                    effect instanceof bw.development.facelessItems.Effects.DamageMultiplierEffect)
            {
                potionAndModifierEffects.add(effect);
            }
        }

        // DEBUG D: Total final de pasivas a procesar
        //plugin.getLogger().info("DEBUG CHECKER: Total de pasivas acumuladas para Applier: " + allPassiveEffects.size());

        // 3. ¡DELEGAR LA APLICACIÓN AL APPLIER!
        passiveEffectApplier.applyEffects(player, allPassiveEffects, activeSetEffects);
    }

    private void applyGrowthBoost(Player player, double radius, double chance) {
        if (Math.random() > chance) {
            return; // Fallo en el chequeo de probabilidad
        }

        // Iteramos sobre todos los bloques en el radio
        Location center = player.getLocation();
        int r = (int) Math.ceil(radius); // Usamos Math.ceil para asegurar que el radio cubra el bloque completo

        for (int x = -r; x <= r; x++) {
            for (int y = -r; y <= r; y++) {
                for (int z = -r; z <= r; z++) {

                    // Obtenemos el bloque en la posición relativa
                    org.bukkit.block.Block block = center.clone().add(x, y, z).getBlock();

                    // CRÍTICO: Si el bloque es un cultivo y tiene una edad (Ageable), simulamos un 'tick' de crecimiento.
                    if (block.getBlockData() instanceof org.bukkit.block.data.Ageable ageable) {

                        // Solo aplica si el bloque NO está en su máxima edad
                        if (ageable.getAge() < ageable.getMaximumAge()) {

                            // Forzamos el crecimiento de forma segura:
                            // Simulamos que el bloque recibe un 'tick' aleatorio de forma manual
                            org.bukkit.World world = block.getWorld();

                            // Usamos el método nativo para forzar el crecimiento o la verificación de tick
                            // La opción más limpia es simular un crecimiento:
                            block.applyBoneMeal(org.bukkit.block.BlockFace.UP);

                            // Nota: applyBoneMeal es la forma más limpia, pero gasta hueso si la API lo permite.
                            // Alternativa: block.getState().update(true); (Menos confiable para crecimiento)
                        }
                    }
                }
            }
        }
    }

    // Método para limpiar el rastro del jugador al desconectarse.
    public void playerQuit(Player player) {
        activeSetEffects.remove(player.getUniqueId());
    }
}