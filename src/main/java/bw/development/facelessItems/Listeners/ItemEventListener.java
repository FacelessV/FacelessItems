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
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemEventListener implements Listener {

    private final FacelessItems plugin;

    public ItemEventListener(FacelessItems plugin) {
        this.plugin = plugin;
    }

    /**
     * Listens when a player damages an entity (hits it).
     * Applies effects with the "on_hit" trigger.
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || weapon.getType().isAir()) return;

        // Get the CustomItem from the ItemStack.
        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(weapon);
        if (customItem == null) return;

        // Get the effects for the "on_hit" trigger.
        List<Effect> effects = customItem.getEffects("on_hit");
        if (effects.isEmpty()) return;

        // Create the context for the effect.
        Entity target = event.getEntity();
        EffectContext context = new EffectContext(
                player,
                target,
                event,
                Collections.singletonMap("damage_amount", event.getDamage())
        );

        // Apply all effects.
        for (Effect effect : effects) {
            effect.apply(context);
        }
    }

    /**
     * Listens when a player interacts (right-click, for example).
     * Applies effects with the "on_use" trigger.
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType().isAir()) return;

        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(item);
        if (customItem == null) return;

        List<Effect> effects = customItem.getEffects("on_use");
        if (effects.isEmpty()) return;

        // Create the context for the effect (the target entity is null in this case).
        EffectContext context = new EffectContext(
                player,
                null,
                event,
                Collections.emptyMap()
        );

        // Apply all effects.
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
}