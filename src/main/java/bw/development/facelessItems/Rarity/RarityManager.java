package bw.development.facelessItems.Rarity;

import bw.development.facelessItems.FacelessItems;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.ChatColor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RarityManager {

    private final FacelessItems plugin;
    private final Map<String, Rarity> rarities = new HashMap<>();

    public RarityManager(FacelessItems plugin) {
        this.plugin = plugin;
    }

    public void loadRarities() {
        rarities.clear();
        File folder = new File(plugin.getDataFolder(), "rarity"); // <-- Directorio corregido aquí
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.saveResource("rarities/legendary.yml", false);
        }

        File[] files = folder.listFiles((f, n) -> n.endsWith(".yml"));
        if (files == null || files.length == 0) {
            plugin.getLogger().warning("No se encontraron archivos de rarezas en la carpeta /rarity/");
            return;
        }

        for (File file : files) {
            try {
                String id = file.getName().replace(".yml", "").toUpperCase();
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                String name = ChatColor.translateAlternateColorCodes('&', config.getString("name", "&f" + id));
                String color = ChatColor.translateAlternateColorCodes('&', config.getString("color", "&f"));
                String loreTag = ChatColor.translateAlternateColorCodes('&', config.getString("lore-tag", ""));

                if (color.equals("&f")) {
                    color = ChatColor.translateAlternateColorCodes('&', config.getString("color", "&f"));
                }
                if (loreTag.isEmpty()) {
                    loreTag = ChatColor.translateAlternateColorCodes('&', config.getString("lore-tag", ""));
                }

                rarities.put(id, new Rarity(id, name, color, loreTag));
                plugin.getLogger().info("Rareza '" + id + "' cargada con éxito.");
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar rareza desde archivo " + file.getName(), e);
            }
        }
        plugin.getLogger().info("Se cargaron " + rarities.size() + " rarezas.");
    }

    public Rarity getRarity(String id) {
        if (id == null) return null;
        return rarities.get(id.toUpperCase());
    }
}