package bw.development.facelessItems.Listeners;

import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Effects.Effect;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ItemEventListener implements Listener {

    private final FacelessItems plugin;

    public ItemEventListener(FacelessItems plugin) {
        this.plugin = plugin;
    }

    /**
     * Escucha cuando un jugador daña a una entidad (golpea).
     * Aplica efectos con trigger "on_hit".
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon == null || !weapon.hasItemMeta()) return;
        ItemMeta meta = weapon.getItemMeta();
        if (meta == null) return;

        // Obtener el CustomItem asociado al ItemStack
        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(weapon);
        if (customItem == null) return;

        // Obtener los efectos para el trigger "on_hit"
        List<Effect> effects = customItem.getEffects("on_hit");
        if (effects == null) return;

        // Aplicar todos los efectos al jugador y evento actual
        for (Effect effect : effects) {
            effect.apply(player, event);
        }
    }

    /**
     * Escucha cuando un jugador interactúa (clic derecho, por ejemplo).
     * Aplica efectos con trigger "on_use".
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        // plugin.getLogger().info("Interacción detectada con: " + player.getName());

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByItemStack(item);
        if (customItem == null) {
            // plugin.getLogger().info("No es un custom item.");
            return;
        }

        plugin.getLogger().info("Item personalizado detectado: " + customItem.getKey());

        List<Effect> effects = customItem.getEffects("on_use");
        if (effects == null) {
            plugin.getLogger().info("No hay efectos on_use.");
            return;
        }

        for (Effect effect : effects) {
            plugin.getLogger().info("Aplicando efecto: " + effect.getClass().getSimpleName());
            effect.apply(player, null);
        }
    }



    // Puedes agregar más métodos para otros triggers, como onConsume, onDrop, etc.
}
