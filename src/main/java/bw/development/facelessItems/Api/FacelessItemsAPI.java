package bw.development.facelessItems.Api;

import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Items.CustomItemManager;
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

    /**
     * Obtiene una copia del ItemStack de un ítem personalizado por su clave.
     * @param key La clave (ID) del ítem (ej: "pico_dragon").
     * @return Una copia del ItemStack si se encuentra, o null si no existe.
     */
    @Nullable
    public static ItemStack getItem(String key) {
        if (instance == null || key == null) {
            return null;
        }

        CustomItem customItem = instance.customItemManager.getCustomItemByKey(key);
        if (customItem == null) {
            return null;
        }

        // Devolvemos un clon para seguridad.
        return customItem.getItemStack().clone();
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