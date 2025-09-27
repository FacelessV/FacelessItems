package bw.development.facelessItems.Listeners;

import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.Effects.MessageEffect;
import bw.development.facelessItems.Effects.MultiShotEffect;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Effects.Effect;
import bw.development.facelessItems.Effects.EffectContext;
import bw.development.facelessItems.Items.CustomItemManager;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEventListener implements Listener {

    private final FacelessItems plugin;
    private final CustomItemManager customItemManager;
    private boolean isApplyingCustomDamage = false;

    // Creamos las claves una sola vez para ser más eficientes
    private final NamespacedKey bowKey;
    private final NamespacedKey arrowKey;

    public ItemEventListener(FacelessItems plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
        this.bowKey = new NamespacedKey(plugin, "custom_arrow_from_bow");
        this.arrowKey = new NamespacedKey(plugin, "custom_arrow_itself");
    }

    // --- NUEVO MÉTODO PARA 'on_damage_taken' ---
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        // 1. Solo nos interesa si la entidad dañada es un jugador
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // 2. Buscamos al atacante (si existe)
        Entity attacker = null;
        if (event instanceof EntityDamageByEntityEvent damageByEntityEvent) {
            attacker = damageByEntityEvent.getDamager();
        }

        // 3. Iteramos sobre las 4 piezas de armadura del jugador
        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            if (armorPiece == null || armorPiece.getType().isAir()) {
                continue;
            }

            CustomItem customItem = customItemManager.getCustomItemByItemStack(armorPiece);
            if (customItem == null) {
                continue;
            }

            // 4. Obtenemos los efectos para el trigger 'on_damage_taken'
            List<Effect> effects = customItem.getEffects("on_damage_taken");
            if (effects.isEmpty()) {
                continue;
            }

            // 5. Creamos el contexto para este evento
            Map<String, Object> data = new HashMap<>();
            data.put("damage_amount", event.getDamage());

            // Aquí, 'user' es el jugador que recibe el daño y 'targetEntity' es el atacante
            EffectContext context = new EffectContext(
                    player,
                    attacker,
                    event,
                    data,
                    customItem.getKey(),
                    plugin
            );

            // 6. Aplicamos cada efecto
            for (Effect effect : effects) {
                effect.apply(context);
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

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        CustomItem customItem = customItemManager.getCustomItemByItemStack(item);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_mine");
        if (effects.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("broken_block", event.getBlock());

        EffectContext context = new EffectContext(
                player,
                null,
                event,
                data,
                customItem.getKey(),
                plugin
        );
        for (Effect effect : effects) {
            effect.apply(context);
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
        if (!(event.getEntity() instanceof Player player) || !(event.getProjectile() instanceof Arrow)) {
            return;
        }

        ItemStack bow = event.getBow();
        CustomItem customBow = customItemManager.getCustomItemByItemStack(bow);
        if (customBow == null) {
            return;
        }

        // Buscamos el efecto MULTI_SHOT
        MultiShotEffect multiShotEffect = customBow.getEffects("on_bow_shoot").stream()
                .filter(MultiShotEffect.class::isInstance)
                .map(MultiShotEffect.class::cast)
                .findFirst().orElse(null);

        if (multiShotEffect != null) {
            String cooldownId = multiShotEffect.getCooldownId() != null ? multiShotEffect.getCooldownId() : customBow.getKey();

            // 1. Comprobamos el cooldown de forma explícita PRIMERO
            if (multiShotEffect.getCooldown() > 0 && plugin.getCooldownManager().isOnCooldown(player, cooldownId)) {
                long remaining = plugin.getCooldownManager().getRemainingCooldown(player, cooldownId);
                String formatted = String.format("%.1f", remaining / 1000.0);
                plugin.getMessageManager().sendMessage(player, "item_on_cooldown", "{cooldown_remaining}", formatted);
                event.setCancelled(true); // Cancelamos el disparo si está en cooldown
                return;
            }

            // 2. Comprobamos las condiciones de forma explícita
            EffectContext context = new EffectContext(player, null, event, Collections.emptyMap(), customBow.getKey(), plugin);
            for (Condition condition : multiShotEffect.getConditions()) { // Asumiendo un getter para conditions en BaseEffect
                if (!condition.check(context)) {
                    // Si una condición falla, no hacemos nada. La flecha se dispara normalmente.
                    tagArrow((Arrow) event.getProjectile(), customBow, event.getConsumable(), true);
                    return;
                }
            }

            // 3. Si todo pasa, APLICAMOS el cooldown y el efecto
            if (multiShotEffect.getCooldown() > 0) {
                plugin.getCooldownManager().setCooldown(player, cooldownId, multiShotEffect.getCooldown());
            }

            event.setCancelled(true); // Cancelamos el disparo original para reemplazarlo

            int arrowCount = multiShotEffect.arrowCount;
            double spread = multiShotEffect.spread;
            ItemStack arrowItem = event.getConsumable();

            for (int i = 0; i < arrowCount; i++) {
                double angle = (i - (arrowCount - 1) / 2.0) * spread;
                Vector rotatedDirection = player.getEyeLocation().getDirection().clone().rotateAroundY(Math.toRadians(angle));

                Arrow newArrow = player.launchProjectile(Arrow.class, rotatedDirection);
                newArrow.setPickupStatus(Arrow.PickupStatus.CREATIVE_ONLY);

                if (multiShotEffect.copyCustomArrowMeta) { /* ... tu lógica para copiar meta ... */ }
                tagArrow(newArrow, customBow, arrowItem, multiShotEffect.propagateArrowEffects);
            }

        } else {
            // Si no hay efecto MULTI_SHOT, etiquetamos la flecha normalmente
            tagArrow((Arrow) event.getProjectile(), customBow, event.getConsumable(), true);
        }
    }

    private void tagArrow(Arrow arrow, CustomItem customBow, ItemStack arrowItem, boolean shouldTagCustomArrow) {
        if (customBow != null) {
            arrow.getPersistentDataContainer().set(bowKey, PersistentDataType.STRING, customBow.getKey());
        }
        if (shouldTagCustomArrow) {
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
}