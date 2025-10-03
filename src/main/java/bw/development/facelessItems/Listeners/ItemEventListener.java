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

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        Entity attacker = null;
        if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            attacker = damageByEntityEvent.getDamager();
        }

        // --- LÓGICA EXISTENTE PARA EFECTOS DE ÍTEMS INDIVIDUALES ---
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            CustomItem customItem = customItemManager.getCustomItemByItemStack(armorPiece);
            if (customItem != null) {
                List<Effect> effects = customItem.getEffects("on_damage_taken");
                if (!effects.isEmpty()) {
                    EffectContext context = new EffectContext(player, attacker, event, Map.of("damage_amount", event.getDamage()), customItem.getKey(), plugin);
                    for (Effect effect : effects) {
                        effect.apply(context);
                    }
                }
            }
        }

        // --- ¡NUEVA LÓGICA PARA LOS BONUS DE SET! ---
        SetManager setManager = plugin.getSetManager();
        if (setManager == null) return;

        // 1. Contamos cuántas piezas de cada set lleva el jugador
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

        // 2. Iteramos sobre los sets que el jugador tiene equipados
        for (Map.Entry<ArmorSet, Integer> entry : setPiecesCount.entrySet()) {
            ArmorSet currentSet = entry.getKey();
            int pieceCount = entry.getValue();

            // 3. Obtenemos el bonus correspondiente al número de piezas
            ArmorSetBonus bonus = currentSet.getBonus(pieceCount);
            if (bonus != null) {
                // 4. Buscamos si este bonus tiene efectos para el trigger 'on_damage_taken'
                List<BaseEffect> triggeredEffects = bonus.getTriggeredEffects().get("on_damage_taken");
                if (triggeredEffects != null && !triggeredEffects.isEmpty()) {

                    // Creamos un contexto para los efectos del set
                    EffectContext setContext = new EffectContext(player, attacker, event, Map.of("damage_amount", event.getDamage()), currentSet.getKey(), plugin);

                    // 5. Aplicamos los efectos del set
                    for (Effect effect : triggeredEffects) {
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

        try {
            isApplyingCustomDamage = true;
            Entity target = event.getEntity();
            // Aquí, 'user' es el atacante y 'targetEntity' es el que recibe el daño
            EffectContext context = new EffectContext(
                    player,
                    target,
                    event,
                    Collections.singletonMap("damage_amount", event.getDamage()),
                    customItem.getKey(),
                    plugin
            );
            for (Effect effect : effects) {
                effect.apply(context);
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

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
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
            effect.apply(context);
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

        // --- 1. BÚSQUEDA DE SMELT (CON GARANTÍA 'FINAL') ---

        // Declaramos la variable principal como 'final' para que pueda ser usada en la lambda.
        final SmeltEffect smeltEffect;

        // Usamos una variable temporal para realizar la búsqueda.
        SmeltEffect tempSmeltEffect = null;

        // A. Buscar en el trigger Pasivo (on_mine)
        tempSmeltEffect = customItem.getEffects("on_mine").stream()
                .filter(SmeltEffect.class::isInstance)
                .map(SmeltEffect.class::cast)
                .findFirst().orElse(null);

        // B. Si no se encuentra, buscar en el trigger Activo (on_use, dentro del Chain)
        if (tempSmeltEffect == null) {
            List<Effect> onUseEffects = customItem.getEffects("on_use");

            // Asumimos que el primer efecto en on_use es el CHAIN
            if (!onUseEffects.isEmpty() && onUseEffects.get(0) instanceof ChainEffect chainEffect) {

                // Buscamos el SmeltEffect DENTRO de los efectos encadenados
                // (Asume que getChainedEffects() ya se añadió a ChainEffect.java)
                tempSmeltEffect = chainEffect.getChainedEffects().stream()
                        .filter(SmeltEffect.class::isInstance)
                        .map(SmeltEffect.class::cast)
                        .findFirst().orElse(null);
            }
        }

        // Asignamos el resultado final a la variable 'final'
        smeltEffect = tempSmeltEffect;

        // Si no se encontró en ninguno de los dos lugares, no fundimos nada.
        if (smeltEffect == null) return;

        // --- 2. VERIFICACIÓN DE CONDICIONES ---
        boolean conditionsMet = true;
        EffectContext context = new EffectContext(player, null, event, new HashMap<>(Map.of("broken_block", event.getBlock())), customItem.getKey(), plugin);

        for (Condition condition : smeltEffect.getConditions()) {
            if (condition instanceof BlockCondition blockCond) {
                if (!blockCond.matches(event.getBlockState().getType())) {
                    conditionsMet = false;
                    break;
                }
            } else {
                if (!condition.check(context)) {
                    conditionsMet = false;
                    break;
                }
            }
        }

        if (!conditionsMet) return;

        // --- 3. APLICACIÓN DE EXPERIENCIA ---
        if (smeltEffect.dropExperience) {
            int exp = smeltEffect.getExperience(event.getBlockState().getType());
            if (exp > 0) {
                event.getBlock().getWorld().spawn(event.getBlock().getLocation().add(0.5, 0.5, 0.5), ExperienceOrb.class, orb -> orb.setExperience(exp));
            }
        }

        // --- 4. SUSTITUCIÓN DE ÍTEMS (Lógica que faltaba) ---
        List<org.bukkit.entity.Item> drops = event.getItems();

        drops.removeIf(itemEntity -> {
            ItemStack itemStack = itemEntity.getItemStack();

            // La variable 'smeltEffect' es final, por lo que su uso aquí es seguro.
            Material smeltedMaterial = smeltEffect.getSmeltedResult(itemStack.getType());

            if (smeltedMaterial != null) {
                // Creamos el nuevo ItemStack fundido
                ItemStack smeltedStack = new ItemStack(smeltedMaterial, itemStack.getAmount());

                // Lo dropeamos al mundo
                event.getBlock().getWorld().dropItemNaturally(itemEntity.getLocation(), smeltedStack);

                // Retorna TRUE para ELIMINAR el ítem original (el mineral)
                return true;
            }

            return false; // Mantiene el ítem sin fundir
        });
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
}