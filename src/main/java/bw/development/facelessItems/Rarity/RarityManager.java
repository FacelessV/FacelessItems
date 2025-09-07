package bw.development.facelessItems.Rarity;

import bw.development.facelessItems.FacelessItems;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RarityManager {

    private final FacelessItems plugin;
    private final Map<String, Rarity> rarities = new HashMap<>();

    public RarityManager(FacelessItems plugin) {
        this.plugin = plugin;
    }

    public void loadRarities() {
        rarities.clear();
        File folder = new File(plugin.getDataFolder(), "rarity");
        if (!folder.exists()) folder.mkdirs();

        File[] files = folder.listFiles((f, n) -> n.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("No se encontraron archivos de rarezas en /rarity/");
            return;
        }

        for (File file : files) {
            try {
                String id = file.getName().replace(".yml", "").toUpperCase();
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                String name = config.getString("name", "&f" + id);
                String color = config.getString("color", "&f");
                String loreTag = config.getString("lore-tag", "");

                rarities.put(id, new Rarity(id, name, color, loreTag));
            } catch (Exception e) {
                plugin.getLogger().warning("Error al cargar rareza desde archivo " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Se cargaron " + rarities.size() + " rarezas.");
    }

    public Rarity getRarity(String id) {
        if (id == null) return null;
        return rarities.get(id.toUpperCase());
    }
}
