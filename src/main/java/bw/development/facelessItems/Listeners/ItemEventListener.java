package bw.development.facelessItems.Listeners;

import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Effects.Effect;
import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEventListener implements Listener {

    private final FacelessItems plugin;
    private boolean isApplyingCustomDamage = false; // <-- NUEVA BANDERA

    public ItemEventListener(FacelessItems plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        // Si ya estamos aplicando daño personalizado, salimos para evitar el bucle.
        if (isApplyingCustomDamage) {
            return;
        }

        if (event.getCause() != DamageCause.ENTITY_ATTACK) {
            return;
        }

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir()) return;

        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(weapon);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_hit");
        if (effects.isEmpty()) return;

        try {
            isApplyingCustomDamage = true; // Establecemos la bandera en true

            Entity target = event.getEntity();
            EffectContext context = new EffectContext(
                    player,
                    target,
                    event,
                    Collections.singletonMap("damage_amount", event.getDamage())
            );

            for (Effect effect : effects) {
                effect.apply(context);
            }
        } finally {
            isApplyingCustomDamage = false; // Nos aseguramos de que siempre se restablezca la bandera
        }
    }

    // ... (El resto de tu código, incluyendo onPlayerInteract y onBlockBreak)
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // NUEVA VERIFICACIÓN: Solo continuar si la acción es un clic derecho
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR &&
                event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) return;

        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(item);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_use");
        if (effects.isEmpty()) return;

        EffectContext context = new EffectContext(
                player,
                null,
                event,
                Collections.emptyMap()
        );

        for (Effect effect : effects) {
            effect.apply(context);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(item);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_mine");
        if (effects.isEmpty()) return;

        // Crear el contexto para el efecto
        Map<String, Object> data = new HashMap<>();
        data.put("broken_block", event.getBlock());

        EffectContext context = new EffectContext(
                player,
                null,
                event,
                data
        );

        for (Effect effect : effects) {
            effect.apply(context);
        }
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getPlayer().getInventory().getItem(EquipmentSlot.HAND);

        if (item == null || item.getType().isAir()) return;

        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(item);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_fish");
        if (effects.isEmpty()) return;

        EffectContext context = new EffectContext(
                player,
                null,
                event,
                Collections.emptyMap()
        );

        for (Effect effect : effects) {
            effect.apply(context);
        }
    }
}