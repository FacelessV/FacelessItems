package bw.development.facelessItems.Gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GuiListener implements Listener {

    private final ItemsGUI itemsGUI;

    public GuiListener(ItemsGUI itemsGUI) {
        this.itemsGUI = itemsGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        // Solo nos interesa si el inventario abierto es nuestro GUI
        if (!title.startsWith("§8FacelessItems - Lista (")) {
            return;
        }

        event.setCancelled(true); // Prevenimos que el jugador pueda tomar/mover los ítems

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR || !clickedItem.hasItemMeta()) {
            return;
        }

        // Lógica de Navegación
        if (clickedItem.getType() == Material.ARROW) {
            // Extraemos el número de página del título del GUI
            int currentPage = Integer.parseInt(title.substring(title.indexOf('(') + 1, title.indexOf('/')));
            String itemName = clickedItem.getItemMeta().getDisplayName();

            if (itemName.contains("Siguiente")) {
                itemsGUI.open(player, currentPage + 1);
            } else if (itemName.contains("Anterior")) {
                itemsGUI.open(player, currentPage - 1);
            }
            return;
        }

        // Si no es un botón de control, es un ítem personalizado
        if (clickedItem.getType() != Material.BLACK_STAINED_GLASS_PANE) {
            player.getInventory().addItem(clickedItem.clone());
            player.sendMessage("§aHas recibido el ítem.");
            player.closeInventory();
        }
    }
}