package bw.development.facelessItems.Items;

import bw.development.facelessItems.Effects.Effect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItem {

    private final String key;                         // Identificador único (por ejemplo nombre de archivo sin extensión)
    private final ItemStack itemStack;                // ItemStack personalizado que será dado al jugador
    private final FileConfiguration config;           // Configuración YAML original, para leer más propiedades si es necesario

    // Mapa con listas de efectos por trigger (ejemplo: "on_hit", "on_use", etc)
    private final Map<String, List<Effect>> effectsByTrigger = new HashMap<>();

    public CustomItem(String key, ItemStack itemStack, FileConfiguration config) {
        this.key = key;
        this.itemStack = itemStack;
        this.config = config;
    }

    public String getKey() {
        return key;
    }

    // Retorna una copia para evitar modificar el ItemStack original
    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Asigna la lista de efectos a un trigger específico
    public void setEffectsForTrigger(String trigger, List<Effect> effects) {
        effectsByTrigger.put(trigger, effects);
    }

    // Obtiene la lista de efectos para un trigger determinado (puede devolver lista vacía o null)
    public List<Effect> getEffects(String trigger) {
        return effectsByTrigger.get(trigger);
    }
}
