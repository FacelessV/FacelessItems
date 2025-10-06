package bw.development.facelessItems.Listeners;

import bw.development.facelessItems.Effects.*;
import bw.development.facelessItems.Effects.Conditions.BlockCondition;
import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Sets.ArmorSet;
import bw.development.facelessItems.Sets.ArmorSetBonus;
import bw.development.facelessItems.Sets.SetManager;
import dev.aurelium.auraskills.api.event.loot.LootDropEvent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class ItemEventListener implements Listener {

    private final FacelessItems plugin;
    private final CustomItemManager customItemManager;
    private boolean isApplyingCustomDamage = false;
    private final Set<UUID> areaEffectUsers = new HashSet<>();

    // Creamos las claves una sola vez para ser más eficientes
    private final NamespacedKey bowKey;
    private final NamespacedKey arrowKey;

    public ItemEventListener(FacelessItems plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
        this.bowKey = new NamespacedKey(plugin, "custom_arrow_from_bow");
        this.arrowKey = new NamespacedKey(plugin, "custom_arrow_itself");
    }

    public Set<UUID> getAreaEffectUsers() {
        return areaEffectUsers;
    }

// ItemEventListener.java

    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Entity attacker = null;
        if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            attacker = damageByEntityEvent.getDamager();
        }

        // =========================================================
        // --- 0. INTERCEPTACIÓN: Lógica de Bloqueo de Escudo (on_shield_block) ---
        // =========================================================
        if (event instanceof EntityDamageByEntityEvent damageByEntityEvent && attacker != null) {

            // 1. Verificar si el jugador está en la animación de bloqueo
            if (player.isBlocking()) {

                // 2. Verificar si el daño fue cancelado o reducido a cero (bloqueo exitoso de Bukkit)
                if (event.getDamage() == 0.0 || event.isCancelled()) {

                    // Obtener todos los ítems relevantes (armadura + ambas manos)
                    List<ItemStack> allEquipment = new ArrayList<>();
                    allEquipment.addAll(Arrays.asList(player.getInventory().getArmorContents()));
                    allEquipment.add(player.getInventory().getItemInMainHand());
                    allEquipment.add(player.getInventory().getItemInOffHand());

                    // Recorrer el equipo para activar el nuevo trigger "on_shield_block"
                    for (ItemStack item : allEquipment) {
                        CustomItem customItem = customItemManager.getCustomItemByItemStack(item);
                        if (customItem != null) {
                            List<Effect> effects = customItem.getEffects("on_shield_block");

                            for (Effect effect : effects) {
                                // Creamos el contexto con el atacante y el evento de daño original
                                EffectContext blockContext = new EffectContext(
                                        player,
                                        attacker,
                                        event,
                                        Collections.emptyMap(),
                                        customItem.getKey(),
                                        plugin
                                );
                                effect.apply(blockContext);
                            }
                        }
                    }

                    // Nota: No se retorna, permitiendo que las Fases 1 y 2 ejecuten si hay
                    // efectos pasivos que deben chequearse incluso si el daño es 0.0.
                }
            }
        }
        // =========================================================
        // --- FIN 0. INTERCEPTACIÓN ---
        // =========================================================


        double finalDamage = event.getDamage(); // Inicializamos el daño a modificar

        // --- FASE 1: APLICACIÓN DE MODIFICADORES (DEFENSA) ---
        // Recorremos el equipo para aplicar modificadores de daño pasivos.
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            CustomItem customItem = customItemManager.getCustomItemByItemStack(armorPiece);
            if (customItem != null) {
                List<Effect> effects = customItem.getEffects("on_damage_taken");
                if (!effects.isEmpty()) {

                    for (Effect effect : effects) {
                        // --- APLICAR MULTIPLICADOR DE DAÑO ---
                        if (effect instanceof DamageMultiplierEffect dmgMultiplier) {
                            // Creamos un contexto limpio para chequear condiciones
                            EffectContext modContext = new EffectContext(player, attacker, event, Collections.emptyMap(), customItem.getKey(), plugin);

                            if (dmgMultiplier.getConditions().stream().allMatch(c -> c.check(modContext))) {
                                // Aplicamos el multiplicador al daño (Ej: 0.5 para reducir el daño a la mitad)
                                finalDamage *= dmgMultiplier.getMultiplier();
                            }
                        }
                    }
                }
            }
        }

        // --- APLICACIÓN DE MODIFICADORES DE SET BONUS (Si existen) ---
        // (La lógica de Bonus de Set iría aquí si tuviera DamageMultiplierEffect)

        // --- FINAL DE LA FASE 1 ---
        // Aplicamos el daño final modificado a Bukkit antes de ejecutar las acciones.
        event.setDamage(finalDamage);


        // --- FASE 2: APLICACIÓN DE ACCIÓN (EXPLOSION, POTION, etc.) ---
        // Ahora ejecutamos los efectos de acción que usan el daño modificado.

        // 2.1 ÍTEMS INDIVIDUALES
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            CustomItem customItem = customItemManager.getCustomItemByItemStack(armorPiece);
            if (customItem != null) {
                List<Effect> effects = customItem.getEffects("on_damage_taken");

                for (Effect effect : effects) {
                    // Solo ejecutamos efectos que NO sean modificadores
                    if (!(effect instanceof DamageMultiplierEffect)) {
                        // Pasamos el daño FINAL modificado al contexto
                        EffectContext actionContext = new EffectContext(
                                player,
                                attacker,
                                event,
                                Map.of("damage_amount", finalDamage), // Usamos el daño final calculado
                                customItem.getKey(),
                                plugin
                        );
                        effect.apply(actionContext);
                    }
                }
            }
        }

        // 2.2 BONUS DE SET
        SetManager setManager = plugin.getSetManager();
        if (setManager == null) return;

        // Contamos las piezas de nuevo (mantenemos tu lógica de conteo)
        Map<ArmorSet, Integer> setPiecesCount = new HashMap<>();
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            CustomItem customItem = customItemManager.getCustomItemByItemStack(armorPiece);
            if (customItem != null) {
                for (ArmorSet set : setManager.getArmorSets()) {
                    if (set.containsItem(customItem.getKey())) {
                        setPiecesCount.put(set, setPiecesCount.getOrDefault(set, 0) + 1);
                    }
                }
            }
        }

        // Ejecución de acciones del Set Bonus
        for (Map.Entry<ArmorSet, Integer> entry : setPiecesCount.entrySet()) {
            ArmorSet currentSet = entry.getKey();
            int pieceCount = entry.getValue();

            ArmorSetBonus bonus = currentSet.getBonus(pieceCount);
            if (bonus != null) {
                List<BaseEffect> triggeredEffects = bonus.getTriggeredEffects().get("on_damage_taken");
                if (triggeredEffects != null && !triggeredEffects.isEmpty()) {

                    // Pasamos el daño FINAL modificado al contexto del set
                    EffectContext setContext = new EffectContext(player, attacker, event, Map.of("damage_amount", finalDamage), currentSet.getKey(), plugin);

                    for (Effect effect : triggeredEffects) {
                        // Aquí asumimos que los efectos del set son solo ACCIÓN (no modificadores)
                        effect.apply(setContext);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (isApplyingCustomDamage) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(weapon);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_hit");
        if (effects.isEmpty()) return;

        Entity target = event.getEntity();
        double finalDamage = event.getDamage(); // Daño base de Bukkit

        try {
            isApplyingCustomDamage = true;

            // 1. APLICACIÓN DE MODIFICADORES PASIVOS (DamageMultiplier)
            for (Effect effect : effects) {

                // --- APLICAR MULTIPLICADOR DE DAÑO ---
                if (effect instanceof DamageMultiplierEffect dmgMultiplier) {
                    EffectContext modContext = new EffectContext(player, target, event, Collections.emptyMap(), customItem.getKey(), plugin);
                    if (dmgMultiplier.getConditions().stream().allMatch(c -> c.check(modContext))) {
                        finalDamage *= dmgMultiplier.getMultiplier();
                    }
                }
            }

            // 2. SETEAR EL DAÑO FINAL MODIFICADO
            event.setDamage(finalDamage);

            // 3. PREPARAR EL CONTEXTO PARA LOS EFECTOS DE ACCIÓN (INCLUYENDO LIFESTEAL)
            EffectContext actionContext = new EffectContext(
                    player,
                    target,
                    event,
                    // ¡Clave! Pasamos el DAÑO ORIGINAL (event.getDamage()) para que Lifesteal lo use
                    // antes de que Bukkit lo aplique.
                    Collections.singletonMap("damage_amount", event.getDamage()),
                    customItem.getKey(),
                    plugin
            );

            // 4. EJECUTAR EFECTOS DE ACCIÓN (LIFESTEAL, LIGHTNING, etc.)
            for (Effect effect : effects) {
                // Lifesteal ahora se ejecuta como un efecto normal de acción
                if (!(effect instanceof DamageMultiplierEffect)) {
                    effect.apply(actionContext);
                }
            }

        } finally {
            isApplyingCustomDamage = false;
        }
    }

    // --- (El resto de tus métodos 'onPlayerInteract', 'onBlockBreak', etc. no necesitan cambios) ---

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
                event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(item);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_use");
        if (effects.isEmpty()) return;

        EffectContext context = new EffectContext(
                player,
                null,
                event,
                Collections.emptyMap(),
                customItem.getKey(),
                plugin
        );
        for (Effect effect : effects) {
            effect.apply(context);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (areaEffectUsers.contains(event.getPlayer().getUniqueId())) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(item);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_mine");
        if (effects.isEmpty()) return;

        // Buscamos el efecto de minería principal
        BaseEffect mainMiningEffect = effects.stream()
                .filter(e -> e instanceof BreakBlockEffect || e instanceof VeinMineEffect)
                .map(BaseEffect.class::cast)
                .findFirst().orElse(null);

        SmeltEffect smeltModifier = effects.stream().filter(SmeltEffect.class::isInstance).map(SmeltEffect.class::cast).findFirst().orElse(null);
        ReplantEffect replantModifier = effects.stream().filter(ReplantEffect.class::isInstance).map(ReplantEffect.class::cast).findFirst().orElse(null);

        // --- LÓGICA DE REPLANTACIÓN STANDALONE ---
        // Si solo hay un modificador REPLANT y no hay efecto de área, lo ejecutamos.
        if (replantModifier != null && mainMiningEffect == null) {
            EffectContext context = new EffectContext(player, null, event, Map.of("broken_block", event.getBlock()), customItem.getKey(), plugin);

            // Verificamos si es un cultivo y si cumple las condiciones del modificador
            if (replantModifier.getConditions().stream().allMatch(c -> c.check(context)) && event.getBlock().getBlockData() instanceof Ageable) {
                // Ejecutamos la lógica de replantación
                breakAndReplantStandalone(event.getBlock(), plugin);
            }
        }

        // Si no hay efecto de minería principal, salimos. Los modificadores ya fueron manejados:
        // REPLANT (si aplica) se ejecutó, y SMELT actuará en onBlockDropItem.
        if (mainMiningEffect == null) return;

        // Comprobamos si el efecto principal quiere disparar eventos (necesario para Smelt y otros plugins)
        boolean triggerEvents = false;
        if (mainMiningEffect instanceof BreakBlockEffect be) {
            triggerEvents = be.shouldTriggerEvents();
        } else if (mainMiningEffect instanceof VeinMineEffect ve) {
            triggerEvents = ve.shouldTriggerEvents();
        }

        // --- LÓGICA DE EJECUCIÓN DEL EFECTO DE ÁREA ---
        if (triggerEvents) {
            // MODO ESTADÍSTICAS: Activamos el guardia de recursión antes de la rotura.
            areaEffectUsers.add(player.getUniqueId());
            try {
                // Ejecutamos los efectos de minería (inyecta modificadores y aplica el efecto principal).
                runMineEffects(effects, player, event, customItem, smeltModifier, replantModifier);
            } finally {
                // Desactivamos el guardia SIEMPRE.
                areaEffectUsers.remove(player.getUniqueId());
            }
        } else {
            // MODO MODIFICADORES: Ejecución directa sin guardia ni player.breakBlock().
            runMineEffects(effects, player, event, customItem, smeltModifier, replantModifier);
        }
    }


    // Nuevo método de ayuda para no repetir el código de 'onBlockBreak'
    private void runMineEffects(List<Effect> effects, Player player, BlockBreakEvent event, CustomItem customItem, SmeltEffect smeltModifier, ReplantEffect replantModifier) {

        // Contexto para todos los efectos
        EffectContext context = new EffectContext(player, null, event, new HashMap<>(Map.of("broken_block", event.getBlock())), customItem.getKey(), plugin);

        for (Effect effect : effects) {
            if (effect instanceof BreakBlockEffect breakBlock) {
                // Inyectar solo Replant, ya que Smelt actúa en el listener.
                breakBlock.setReplantModifier(replantModifier);
                effect.apply(context);
            } else if (effect instanceof VeinMineEffect veinMine) {
                // Inyectar Smelt (aunque el VeinMine ya no lo usa internamente) y Replant.
                veinMine.setSmeltModifier(smeltModifier);
                veinMine.setReplantModifier(replantModifier);
                effect.apply(context);
            } else if (!(effect instanceof SmeltEffect) && !(effect instanceof ReplantEffect)) {
                // Ejecutar todos los demás efectos que no son modificadores.
                effect.apply(context);
            }
        }
    }

// ItemEventListener.java (Versión más limpia y robusta)

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Limitamos la ejecución solo a los estados donde puede haber una interacción significativa.
        if (event.getState() == PlayerFishEvent.State.FISHING ||
                event.getState() == PlayerFishEvent.State.IN_GROUND ||
                event.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {

            ItemStack item = player.getInventory().getItemInMainHand();
            CustomItem customItem = customItemManager.getCustomItemByItemStack(item);
            if (customItem == null) return;

            List<Effect> effects = customItem.getEffects("on_fish");
            if (effects.isEmpty()) return;

            EffectContext context = new EffectContext(
                    player,
                    null,
                    event,
                    Collections.emptyMap(),
                    customItem.getKey(),
                    plugin
            );
            for (Effect effect : effects) {
                // Confiamos en que la lógica de cada Effect (como GrapplingHookEffect)
                // verifica el estado interno de fishEvent.getState()
                effect.apply(context);
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // 1. Verificamos si la entidad fue asesinada por un jugador
        Player killer = event.getEntity().getKiller();
        if (killer == null) {
            return;
        }

        // 2. Obtenemos el arma que usó el jugador
        ItemStack weapon = killer.getInventory().getItemInMainHand();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(weapon);
        if (customItem == null) {
            return;
        }

        // 3. Obtenemos los efectos para el trigger 'on_kill'
        List<Effect> effects = customItem.getEffects("on_kill");
        if (effects.isEmpty()) {
            return;
        }

        // 4. Creamos el contexto: 'user' es el asesino, 'targetEntity' es la entidad que murió
        Map<String, Object> data = new HashMap<>();
        data.put("dropped_exp", event.getDroppedExp());
        // Podríamos añadir los drops aquí si quisiéramos efectos que los modifiquen

        EffectContext context = new EffectContext(
                killer,
                event.getEntity(),
                event,
                data,
                customItem.getKey(),
                plugin
        );

        // 5. Aplicamos los efectos
        for (Effect effect : effects) {
            effect.apply(context);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        ProjectileSource shooter = potion.getShooter();

        // 1. Solo nos interesa si la poción fue lanzada por un jugador
        if (!(shooter instanceof Player thrower)) {
            return;
        }

        // 2. Obtenemos el ítem (la poción) que se lanzó
        ItemStack potionItem = potion.getItem();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(potionItem);
        if (customItem == null) {
            return;
        }

        // 3. Obtenemos los efectos para el trigger 'on_potion_splash'
        List<Effect> effects = customItem.getEffects("on_potion_splash");
        if (effects.isEmpty()) {
            return;
        }

        // 4. ¡Bucle clave! Iteramos sobre CADA entidad afectada por la poción
        for (LivingEntity affectedEntity : event.getAffectedEntities()) {

            // Creamos un contexto individual para cada entidad afectada
            Map<String, Object> data = new HashMap<>();
            data.put("intensity", event.getIntensity(affectedEntity)); // Guardamos la intensidad del efecto

            EffectContext context = new EffectContext(
                    thrower,          // El 'user' es el jugador que lanzó la poción
                    affectedEntity,   // El 'targetEntity' es la entidad afectada actual
                    event,
                    data,
                    customItem.getKey(),
                    plugin
            );

            // Aplicamos todos los efectos a esta entidad específica
            for (Effect effect : effects) {
                effect.apply(context);
            }
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player) || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }

        ItemStack bow = event.getBow();
        CustomItem customBow = customItemManager.getCustomItemByItemStack(bow);
        ItemStack arrowItem = event.getConsumable();
        CustomItem customArrow = customItemManager.getCustomItemByItemStack(arrowItem);

        // Always apply the visual meta to the original arrow entity
        copyArrowMeta(arrowItem, arrow);

        // If the bow isn't custom, we might still need to tag the arrow if it's custom
        if (customBow == null) {
            tagArrow(arrow, null, arrowItem, false, true); // Don't tag a bow, but always tag a custom arrow
            return;
        }

        // From here, we know the bow is custom.
        MultiShotEffect multiShotEffect = customBow.getEffects("on_bow_shoot").stream()
                .filter(MultiShotEffect.class::isInstance)
                .map(MultiShotEffect.class::cast)
                .findFirst().orElse(null);

        if (multiShotEffect != null) {
            // --- MULTI-SHOT LOGIC ---
            String cooldownId = multiShotEffect.getCooldownId() != null ? multiShotEffect.getCooldownId() : customBow.getKey();
            if (multiShotEffect.getCooldown() > 0 && plugin.getCooldownManager().isOnCooldown(player, cooldownId)) {
                // ... (cooldown logic)
                event.setCancelled(true);
                return;
            }

            EffectContext context = new EffectContext(player, null, event, Collections.emptyMap(), customBow.getKey(), plugin);
            boolean conditionsMet = multiShotEffect.getConditions().stream().allMatch(c -> c.check(context));

            if (!conditionsMet) {
                tagArrow(arrow, customBow, arrowItem, true, true);
                return;
            }

            if (multiShotEffect.getCooldown() > 0) {
                plugin.getCooldownManager().setCooldown(player, cooldownId, multiShotEffect.getCooldown());
            }

            event.setCancelled(true);

            int arrowCount = multiShotEffect.arrowCount;
            double spread = multiShotEffect.spread;
            int centerArrowIndex = (arrowCount - 1) / 2;

            for (int i = 0; i < arrowCount; i++) {
                double angle = (i - centerArrowIndex) * spread;
                Vector rotatedDirection = player.getEyeLocation().getDirection().clone().rotateAroundY(Math.toRadians(angle));
                Arrow newArrow = player.launchProjectile(Arrow.class, rotatedDirection);
                newArrow.setPickupStatus(Arrow.PickupStatus.CREATIVE_ONLY);

                if (multiShotEffect.copyCustomArrowMeta) {
                    copyArrowMeta(arrowItem, newArrow);
                }

                boolean isCenterArrow = (i == centerArrowIndex);

                // The bow's effects apply if it's the center arrow OR if propagate_bow_effects is true
                boolean shouldTagBow = isCenterArrow || multiShotEffect.propagateBowEffects;

                // The arrow's effects apply if it's the center arrow OR if propagate_arrow_effects is true
                boolean shouldTagArrow = isCenterArrow || multiShotEffect.propagateArrowEffects;

                tagArrow(newArrow, customBow, arrowItem, shouldTagBow, shouldTagArrow);
            }
        } else {
            // --- SINGLE SHOT LOGIC ---
            // A single shot from a custom bow always tags both.
            tagArrow(arrow, customBow, arrowItem, true, true);
        }
    }

    // Helper method to copy visual properties
    private void copyArrowMeta(ItemStack originalArrowItem, Arrow newArrowEntity) {
        if (originalArrowItem != null && originalArrowItem.getItemMeta() instanceof PotionMeta) {
            PotionMeta originalMeta = (PotionMeta) originalArrowItem.getItemMeta();
            if (originalMeta.hasCustomEffects()) {
                for(org.bukkit.potion.PotionEffect pEffect : originalMeta.getCustomEffects()){
                    newArrowEntity.addCustomEffect(pEffect, true);
                }
            }
            if (originalMeta.hasColor()) {
                newArrowEntity.setColor(originalMeta.getColor());
            }
        }
    }

    // Updated helper method to tag bow and arrow effects separately
    private void tagArrow(Arrow arrow, CustomItem customBow, ItemStack arrowItem, boolean shouldTagBow, boolean shouldTagArrow) {
        if (shouldTagBow && customBow != null) {
            arrow.getPersistentDataContainer().set(bowKey, PersistentDataType.STRING, customBow.getKey());
        }
        if (shouldTagArrow) {
            CustomItem customArrow = customItemManager.getCustomItemByItemStack(arrowItem);
            if (customArrow != null) {
                arrow.getPersistentDataContainer().set(arrowKey, PersistentDataType.STRING, customArrow.getKey());
            }
        }
    }

    // --- MÉTODO onProjectileHit ACTUALIZADO ---
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow) || !(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        boolean effectApplied = false;

        // 1. Comprobamos si la flecha vino de un ARCO personalizado y ejecutamos sus efectos
        if (arrow.getPersistentDataContainer().has(bowKey, PersistentDataType.STRING)) {
            String bowItemKey = arrow.getPersistentDataContainer().get(bowKey, PersistentDataType.STRING);
            CustomItem customBow = customItemManager.getCustomItemByKey(bowItemKey);
            if (customBow != null) {
                runEffects(customBow, "on_arrow_hit", shooter, event);
                effectApplied = true;
            }
        }

        // 2. Comprobamos si la FLECHA misma es personalizada y ejecutamos sus efectos
        if (arrow.getPersistentDataContainer().has(arrowKey, PersistentDataType.STRING)) {
            String arrowItemKey = arrow.getPersistentDataContainer().get(arrowKey, PersistentDataType.STRING);
            CustomItem customArrow = customItemManager.getCustomItemByKey(arrowItemKey);
            if (customArrow != null) {
                runEffects(customArrow, "on_arrow_hit", shooter, event);
                effectApplied = true;
            }
        }

        // Si se aplicó cualquier efecto (del arco o de la flecha), removemos la flecha
        if (effectApplied) {
            arrow.remove();
        }
    }


    /**
     * Método de ayuda para no repetir código. Ejecuta los efectos de un ítem.
     */
    private void runEffects(CustomItem customItem, String trigger, Player shooter, ProjectileHitEvent event) {
        List<Effect> effects = customItem.getEffects(trigger);
        if (effects.isEmpty()) return;

        EffectContext context = new EffectContext(
                shooter,
                event.getHitEntity(),
                event,
                Collections.emptyMap(),
                customItem.getKey(),
                plugin
        );

        for (Effect effect : effects) {
            effect.apply(context);
        }
    }

    @EventHandler
    public void onExpBottleHit(ExpBottleEvent event) {
        ThrownExpBottle bottle = event.getEntity();
        ProjectileSource shooter = bottle.getShooter();

        // Solo nos interesa si fue lanzada por un jugador
        if (!(shooter instanceof Player thrower)) {
            return;
        }

        // Obtenemos el ItemStack de la botella que se lanzó
        ItemStack item = bottle.getItem();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(item);
        if (customItem == null) {
            return;
        }

        // --- LÓGICA FINAL ---

        // 1. Modificamos la experiencia base usando el valor ya cargado en el CustomItem.
        int customExperience = customItem.getCustomExperience();
        if (customExperience >= 0) {
            event.setExperience(customExperience);
        }

        // 2. Ejecutamos los efectos adicionales del trigger 'on_exp_bottle_hit'
        List<Effect> effects = customItem.getEffects("on_exp_bottle_hit");
        if (effects.isEmpty()) {
            return;
        }

        EffectContext context = new EffectContext(
                thrower,
                event.getHitEntity(),
                event,
                Collections.emptyMap(),
                customItem.getKey(),
                plugin
        );

        for (Effect effect : effects) {
            effect.apply(context);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockDropItem(BlockDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(tool);
        if (customItem == null) return;

        // --- SETUP: CONTEXTO Y BÚSQUEDA DE EFECTOS ---
        EffectContext context = new EffectContext(player, null, event, new HashMap<>(Map.of("broken_block", event.getBlock())), customItem.getKey(), plugin);

        // 1. BÚSQUEDA DE SMELT (Modificador de Sustitución)
        // El método auxiliar encuentra el efecto SMELT en on_mine o en el CHAIN.
        final SmeltEffect smeltEffect = findSmeltEffect(customItem);

        // 2. BÚSQUEDA DE LOOT MULTIPLIER (Modificador de Cantidad)
        // El método auxiliar busca el LOOT_MULTIPLIER en el trigger 'on_mine'.
        final LootMultiplierEffect lootEffect = findLootMultiplierEffect(customItem);

        // Si no hay ningún modificador relevante, salimos.
        if (smeltEffect == null && lootEffect == null) return;

        // --- 3. PROCESAMIENTO DE SMELT (Sustitución y EXP) ---
        if (smeltEffect != null) {
            // Chequeamos condiciones de fundición contra el bloque roto.
            boolean smeltConditionsMet = checkConditions(smeltEffect, context, event.getBlockState().getType());

            if (smeltConditionsMet) {
                // Aplicación de EXP y Sustitución de Ítems (Lógica de reemplazo)
                applySmeltLogic(smeltEffect, event);
            }
        }

        // --- 4. PROCESAMIENTO DE LOOT MULTIPLIER (Multiplicación de Cantidad) ---
        // Este paso debe ir AL FINAL, ya que actúa sobre los ítems finales (ya fundidos o no).
        if (lootEffect != null) {
            // Chequeamos condiciones para la multiplicación de botín
            boolean lootConditionsMet = checkConditions(lootEffect, context, event.getBlockState().getType());

            if (lootConditionsMet) {
                // Aplicamos el multiplicador a la lista de drops
                applyLootMultiplier(lootEffect, event.getItems());
            }
        }
    }

    // =======================================================================
// --- MÉTODOS AUXILIARES (DEBEN AÑADIRSE A ITEMEVENTLISTENER.JAVA) ---
// =======================================================================

    private SmeltEffect findSmeltEffect(CustomItem customItem) {
        // Busca en on_mine
        SmeltEffect tempSmeltEffect = customItem.getEffects("on_mine").stream()
                .filter(SmeltEffect.class::isInstance)
                .map(SmeltEffect.class::cast)
                .findFirst().orElse(null);

        // Si no se encuentra, busca en el CHAIN de on_use
        if (tempSmeltEffect == null) {
            List<Effect> onUseEffects = customItem.getEffects("on_use");
            if (!onUseEffects.isEmpty() && onUseEffects.get(0) instanceof ChainEffect chainEffect) {
                return chainEffect.getChainedEffects().stream()
                        .filter(SmeltEffect.class::isInstance)
                        .map(SmeltEffect.class::cast)
                        .findFirst().orElse(null);
            }
        }
        return tempSmeltEffect;
    }

    private LootMultiplierEffect findLootMultiplierEffect(CustomItem customItem) {
        return customItem.getEffects("on_mine").stream()
                .filter(LootMultiplierEffect.class::isInstance)
                .map(LootMultiplierEffect.class::cast)
                .findFirst().orElse(null);
    }

    private void applySmeltLogic(SmeltEffect smeltEffect, BlockDropItemEvent event) {
        // 1. Aplicación de Experiencia
        if (smeltEffect.dropExperience) {
            int exp = smeltEffect.getExperience(event.getBlockState().getType());
            if (exp > 0) {
                event.getBlock().getWorld().spawn(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ExperienceOrb.class, orb -> orb.setExperience(exp));
            }
        }

        // 2. Sustitución de ÍTEMS
        event.getItems().removeIf(itemEntity -> {
            ItemStack itemStack = itemEntity.getItemStack();
            Material smeltedMaterial = smeltEffect.getSmeltedResult(itemStack.getType());

            if (smeltedMaterial != null) {
                ItemStack smeltedStack = new ItemStack(smeltedMaterial, itemStack.getAmount());
                event.getBlock().getWorld().dropItemNaturally(itemEntity.getLocation(), smeltedStack);
                return true; // Elimina el original
            }
            return false;
        });
    }

    private void applyLootMultiplier(LootMultiplierEffect lootEffect, List<org.bukkit.entity.Item> drops) {
        double multiplier = lootEffect.getMultiplier();
        for (org.bukkit.entity.Item itemEntity : drops) {
            ItemStack itemStack = itemEntity.getItemStack();

            // Calculamos la nueva cantidad (usando Math.floor para obtener un entero seguro)
            int newAmount = (int) Math.floor(itemStack.getAmount() * multiplier);

            // Si la cantidad es válida y mayor que la original, la aplicamos.
            if (newAmount > itemStack.getAmount() && newAmount > 0) {
                itemStack.setAmount(newAmount);
                itemEntity.setItemStack(itemStack);
            }
        }
    }

    private boolean checkConditions(BaseEffect effect, EffectContext context, Material brokenBlockType) {
        // Si la lista de condiciones es nula o vacía, la condición pasa
        if (effect.getConditions().isEmpty()) return true;

        for (Condition condition : effect.getConditions()) {
            // Manejar el caso especial de BlockCondition (que solo chequea el material del bloque)
            if (condition instanceof BlockCondition blockCond) {
                if (!blockCond.matches(brokenBlockType)) {
                    return false;
                }
            }
            // Chequear todas las demás condiciones que usan el contexto completo
            else if (!condition.check(context)) {
                return false;
            }
        }
        return true;
    }
    /**
     * Runs modifier effects like SMELT or REPLANT when they are standalone.
     */
    private void runStandaloneModifiers(List<Effect> effects, Player player, BlockBreakEvent event, CustomItem customItem) {
        EffectContext context = new EffectContext(
                player,
                null,
                event,
                new HashMap<>(Map.of("broken_block", event.getBlock())),
                customItem.getKey(),
                plugin
        );

        for (Effect effect : effects) {
            if (effect instanceof SmeltEffect || effect instanceof ReplantEffect) {
                effect.apply(context);
            }
        }
    }

    /**
     * Lógica para replantar cultivos sin depender de un efecto de área.
     */
    /**
     * Lógica para replantar cultivos sin depender de un efecto de área.
     */
    private void breakAndReplantStandalone(Block block, FacelessItems plugin) {
        Material cropType = block.getType();

        new BukkitRunnable() {
            @Override
            public void run() {
                // Revisa si la base sigue siendo tierra de cultivo
                if (block.getRelative(BlockFace.DOWN).getType() == Material.FARMLAND) {

                    // 1. Establece el tipo de bloque base (ej: WHEAT)
                    block.setType(cropType);

                    // 2. Obtiene los BlockData actuales del bloque (tipo org.bukkit.block.data.BlockData)
                    BlockData newData = block.getBlockData();

                    // 3. Verifica si ESTOS DATOS DE BLOQUE implementan la interfaz Ageable (de bloque)
                    if (newData instanceof org.bukkit.block.data.Ageable newAgeable) {

                        // 4. Establece la edad a 0 (replantar)
                        newAgeable.setAge(0);

                        // 5. Aplica los datos de bloque modificados (newAgeable es de tipo BlockData)
                        block.setBlockData(newAgeable);
                    }
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    /**
     * Intercepta los drops extras generados por AuraSkills (Luck, habilidades de minería, etc.)
     * y aplica el efecto de fundición si el pico del jugador lo tiene.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onAuraSkillsLootDrop(LootDropEvent event) {
        // 1. Verificación: Solo procesamos drops causados por jugadores
        Player player = event.getPlayer();
        if (player == null) return;

        // Obtenemos el ítem en la mano y el CustomItem
        ItemStack tool = player.getInventory().getItemInMainHand();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(tool);
        if (customItem == null) return;

        // 2. Búsqueda del SmeltEffect
        // Utilizamos la búsqueda en el trigger 'on_mine' como fuente principal
        SmeltEffect smeltEffect = customItem.getEffects("on_mine").stream()
                .filter(SmeltEffect.class::isInstance)
                .map(SmeltEffect.class::cast)
                .findFirst().orElse(null);

        // Si no se encuentra en 'on_mine', realizamos la búsqueda en el CHAIN de 'on_use'
        if (smeltEffect == null) {
            List<Effect> onUseEffects = customItem.getEffects("on_use");
            if (!onUseEffects.isEmpty() && onUseEffects.get(0) instanceof ChainEffect chainEffect) {
                smeltEffect = chainEffect.getChainedEffects().stream()
                        .filter(SmeltEffect.class::isInstance)
                        .map(SmeltEffect.class::cast)
                        .findFirst().orElse(null);
            }
        }

        // Si no tiene efecto de fundición, salimos
        if (smeltEffect == null) return;

        // --- APLICACIÓN DE FUNDICIÓN ---

        // 3. Obtenemos el item que AuraSkills está a punto de soltar
        ItemStack droppedItem = event.getItem();

        if (droppedItem == null || droppedItem.getType().isAir()) {
            return;
        }

        // 4. Verificamos si el item se puede fundir según nuestro mapa SMELT_RESULTS
        Material smeltedMaterial = smeltEffect.getSmeltedResult(droppedItem.getType());

        if (smeltedMaterial != null) {
            // Creamos el nuevo ItemStack fundido con la cantidad original
            ItemStack smeltedStack = new ItemStack(smeltedMaterial, droppedItem.getAmount());

            // 5. ¡REEMPLAZAMOS EL ITEM DEL EVENTO DE AURASKILLS!
            event.setItem(smeltedStack);
        }
    }
    /**
     * Intercepta los eventos de daño para aplicar la resistencia al knockback
     * basada en la armadura equipada por el jugador.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onKnockbackResistOverride(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Si el evento ya fue cancelado, el empuje no ocurrirá, así que salimos.
        if (event.isCancelled()) {
            return;
        }

        // --- Lógica para calcular la resistencia total ---
        double totalResistance = 0.0;

        // Recorrer armadura equipada para encontrar efectos KNOCKBACK_RESIST
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            CustomItem customItem = customItemManager.getCustomItemByItemStack(armorPiece);
            if (customItem != null) {
                // Asumimos que la lógica de pasivas se engancha a "on_damage_taken"
                List<Effect> effects = customItem.getEffects("on_damage_taken");

                for (Effect effect : effects) {
                    if (effect instanceof KnockbackResistEffect kbResist) {

                        // Necesitas crear un EffectContext para chequear condiciones (ej. damage_cause)
                        // Este contexto es simplificado, asume que necesitas el atacante si es un EntityDamageByEntityEvent
                        LivingEntity attacker = (event instanceof EntityDamageByEntityEvent attackEvent)
                                ? (LivingEntity) attackEvent.getDamager() : null;

                        EffectContext modContext = new EffectContext(player, attacker, event, Collections.emptyMap(), customItem.getKey(), plugin);

                        if (kbResist.getConditions().stream().allMatch(c -> c.check(modContext))) {
                            totalResistance += kbResist.getResistance();
                        }
                    }
                }
            }
        }

        // La resistencia máxima es 1.0 (100%)
        totalResistance = Math.min(totalResistance, 1.0);

        // --- Lógica de Anulación y Reescalado (FIX) ---
        if (totalResistance > 0.0) {
            final double resistance = totalResistance;
            final Player p = player;

            // Ejecutamos una tarea retrasada un tick para asegurarnos de que Bukkit ya ha aplicado su vector de velocidad
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Obtenemos el vector de velocidad que Bukkit aplicó (el knockback)
                    Vector appliedVelocity = p.getVelocity();

                    // Si la magnitud es insignificante o cero, salimos.
                    if (appliedVelocity.lengthSquared() < 0.0001) {
                        return;
                    }

                    double originalMagnitude = appliedVelocity.length();
                    // La nueva magnitud es la original multiplicada por el empuje restante.
                    double remainingPush = 1.0 - resistance;
                    double finalMagnitude = originalMagnitude * remainingPush;

                    // Calculamos el factor de escala: (magnitud_final / magnitud_original)
                    double scaleFactor = finalMagnitude / originalMagnitude;

                    // Reescalamos el vector de velocidad
                    Vector reducedVelocity = appliedVelocity.clone().multiply(scaleFactor);

                    // Aplicamos la velocidad reducida al jugador
                    p.setVelocity(reducedVelocity);
                }
                // Debes reemplazar 'plugin' con la instancia real de tu plugin
            }.runTaskLater(plugin, 1L);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(org.bukkit.event.player.PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack consumedItem = event.getItem();

        // 1. Verificar si el ítem consumido es custom
        CustomItem customItem = customItemManager.getCustomItemByItemStack(consumedItem);
        if (customItem == null) {
            return;
        }

        // 2. Obtener y verificar los efectos para el nuevo trigger
        List<Effect> effects = customItem.getEffects("on_consume");
        if (effects.isEmpty()) {
            return;
        }

        // 3. Crear el contexto y aplicar los efectos
        // El targetEntity es null ya que el efecto es sobre el usuario.
        EffectContext context = new EffectContext(
                player,
                null,
                event,
                Collections.singletonMap("consumed_item_key", customItem.getKey()), // Se puede pasar data relevante
                customItem.getKey(),
                plugin
        );

        for (Effect effect : effects) {
            effect.apply(context);
        }
    }
}