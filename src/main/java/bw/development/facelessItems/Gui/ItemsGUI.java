package bw.development.facelessItems.Gui;

import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Items.CustomItemManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemsGUI {

    private final CustomItemManager customItemManager;

    public ItemsGUI(CustomItemManager customItemManager) {
        this.customItemManager = customItemManager;
    }

    public void open(Player player, int page) {
        List<CustomItem> items = new ArrayList<>(customItemManager.getAllCustomItems());

        // 54 slots, dejamos la última fila (9 slots) para controles.
        final int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) items.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1; // Mínimo 1 página

        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        String guiTitle = "§8FacelessItems - Lista (" + page + "/" + totalPages + ")";
        Inventory gui = Bukkit.createInventory(null, 54, guiTitle);

        // Llenar el inventario con los ítems de la página actual
        int startIndex = (page - 1) * itemsPerPage;
        for (int i = 0; i < itemsPerPage; i++) {
            int itemIndex = startIndex + i;
            if (itemIndex < items.size()) {
                gui.setItem(i, items.get(itemIndex).getItemStack());
            } else {
                break;
            }
        }

        // --- Controles de Navegación ---
        ItemStack border = createControlItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 45; i < 54; i++) {
            gui.setItem(i, border);
        }

        // Botón de Página Anterior (slot 48)
        if (page > 1) {
            gui.setItem(48, createControlItem(Material.ARROW, "§aPágina Anterior", "§7Ir a la página " + (page - 1)));
        }

        // Botón de Página Siguiente (slot 50)
        if (page < totalPages) {
            gui.setItem(50, createControlItem(Material.ARROW, "§aPágina Siguiente", "§7Ir a la página " + (page + 1)));
        }

        player.openInventory(gui);
    }

    private ItemStack createControlItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
        }
        return item;
    }
}