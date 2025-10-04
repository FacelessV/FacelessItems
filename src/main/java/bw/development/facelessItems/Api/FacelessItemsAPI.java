package bw.development.facelessItems.Api;

import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Items.CustomItemManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;

/**
 * La clase API pública para FacelessItems.
 * Permite a otros plugins interactuar de forma segura con el sistema de ítems.
 */
public final class FacelessItemsAPI {

    private static FacelessItemsAPI instance;
    private final CustomItemManager customItemManager;

    // El constructor es interno para que solo nuestra clase principal pueda crearlo.
    FacelessItemsAPI(CustomItemManager customItemManager) {
        this.customItemManager = customItemManager;
    }

    // --- MÉTODOS DE UTILIDAD CON CANTIDAD ---

    /**
     * Obtiene una copia del ItemStack de un ítem personalizado por su clave, con una cantidad específica.
     * @param key La clave (ID) del ítem (ej: "pico_dragon").
     * @param amount La cantidad deseada del ítem (debe ser > 0).
     * @return Una copia del ItemStack con la cantidad especificada si se encuentra, o null si no existe o la cantidad es inválida.
     */
    @Nullable
    public static ItemStack getItem(String key, int amount) {
        if (instance == null || key == null || amount <= 0) {
            return null;
        }

        CustomItem customItem = instance.customItemManager.getCustomItemByKey(key);
        if (customItem == null) {
            return null;
        }

        // Devolvemos un clon y establecemos la cantidad.
        ItemStack itemCopy = customItem.getItemStack().clone();
        itemCopy.setAmount(amount);

        return itemCopy;
    }

    /**
     * Remueve una cantidad específica de un ítem custom del inventario de un jugador.
     * @param player El jugador del cual remover el ítem.
     * @param key La clave (ID) del ítem custom a remover (ej: "pico_dragon").
     * @param amount La cantidad a remover.
     * @return true si se removió la cantidad solicitada, false si el jugador no tenía suficiente o la API no está inicializada.
     */
    public static boolean removeItem(Player player, String key, int amount) {
        if (instance == null || player == null || key == null || amount <= 0) {
            return false;
        }

        return instance.customItemManager.takeItemFromInventory(player.getInventory(), key, amount);
    }

    /**
     * Obtiene la clave del ítem custom si el ItemStack es un ítem de FacelessItems.
     * Esto se hace típicamente leyendo un NamespacedKey o metadato del ItemStack.
     * @param itemStack El ItemStack a examinar.
     * @return La clave (String) del ítem custom si se encuentra, o null si no es un ítem custom.
     */
    @Nullable
    public static String getKey(ItemStack itemStack) {
        if (instance == null || itemStack == null) {
            return null;
        }

        // Delegamos la lógica de búsqueda (PersistentDataContainer) al manager.
        return instance.customItemManager.getCustomItemKeyFromItemStack(itemStack);
    }

    /**
     * Método interno para ser llamado solo por la clase principal del plugin.
     * @param customItemManager El gestor de ítems.
     */
    public static void initialize(CustomItemManager customItemManager) {
        if (instance == null) {
            instance = new FacelessItemsAPI(customItemManager);
        }
    }
}