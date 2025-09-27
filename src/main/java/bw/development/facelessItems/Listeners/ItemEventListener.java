package bw.development.facelessItems.Listeners;

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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEventListener implements Listener {

    private final FacelessItems plugin;
    private final CustomItemManager customItemManager;
    private boolean isApplyingCustomDamage = false;

    public ItemEventListener(FacelessItems plugin, CustomItemManager customItemManager) {
        this.plugin = plugin;
        this.customItemManager = customItemManager;
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
        // Solo nos interesa si el que dispara es un jugador y el proyectil es una flecha
        if (!(event.getEntity() instanceof Player player) || !(event.getProjectile() instanceof Arrow arrow)) {
            return;
        }

        ItemStack bow = event.getBow();
        CustomItem customBow = customItemManager.getCustomItemByItemStack(bow);
        if (customBow == null) {
            return;
        }

        // Creamos una clave única para nuestra etiqueta
        NamespacedKey key = new NamespacedKey(plugin, "custom_arrow_from_item");

        // "Etiquetamos" la flecha con la ID del arco que la disparó
        arrow.getPersistentDataContainer().set(key, PersistentDataType.STRING, customBow.getKey());
    }

    // --- MÉTODO 2 (NUEVO): Detectar el impacto de la flecha "etiquetada" ---
    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        // Solo nos interesan las flechas
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }

        // Comprobamos si la flecha tiene nuestra etiqueta
        NamespacedKey key = new NamespacedKey(plugin, "custom_arrow_from_item");
        if (!arrow.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
            return;
        }

        // Obtenemos la ID del arco que la disparó
        String itemKey = arrow.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        CustomItem customBow = customItemManager.getCustomItemByKey(itemKey);
        if (customBow == null) {
            return;
        }

        // Verificamos que el que disparó es un jugador
        if (!(arrow.getShooter() instanceof Player shooter)) {
            return;
        }

        // Obtenemos los efectos para el trigger 'on_arrow_hit'
        List<Effect> effects = customBow.getEffects("on_arrow_hit");
        if (effects.isEmpty()) {
            return;
        }

        // Creamos el contexto. El 'targetEntity' puede ser la entidad golpeada o null si se golpeó un bloque.
        EffectContext context = new EffectContext(
                shooter,
                event.getHitEntity(),
                event,
                Collections.emptyMap(),
                customBow.getKey(),
                plugin
        );

        // Aplicamos los efectos
        for (Effect effect : effects) {
            effect.apply(context);
        }

        // Opcional: removemos la flecha después del impacto para un efecto más limpio
        arrow.remove();
    }

}