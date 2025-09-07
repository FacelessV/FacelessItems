package bw.development.facelessItems.Items;

import bw.development.facelessItems.Effects.Effect;
import bw.development.facelessItems.Effects.EffectFactory;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Rarity.Rarity;
import bw.development.facelessItems.Rarity.RarityManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class CustomItemManager {

    private final FacelessItems plugin;
    private final Map<String, CustomItem> customItems = new HashMap<>();

    public CustomItemManager(FacelessItems plugin) {
        this.plugin = plugin;
    }

    public void loadItems() {
        customItems.clear();
        File itemsFolder = new File(plugin.getDataFolder(), "items");
        if (!itemsFolder.exists()) {
            itemsFolder.mkdirs();
            plugin.saveResource("items/espada_eco.yml", false);
        }

        File[] files = itemsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            try {
                String key = file.getName().replace(".yml", "");
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                String matName = config.getString("material", "STONE").toUpperCase(Locale.ROOT);
                Material material = Material.valueOf(matName);

                String displayName = config.getString("display-name", "Custom Item");

                List<String> lore = config.getStringList("lore").stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());

                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) continue;

                // Leer rareza
                String rarityId = config.getString("rarity", "COMMON").toUpperCase();
                RarityManager rarityManager = plugin.getRarityManager();
                Rarity rarity = rarityManager.getRarity(rarityId);

                // Aplicar color de rareza al nombre
                String finalName = rarity != null
                        ? ChatColor.translateAlternateColorCodes('&', rarity.getColor() + ChatColor.stripColor(displayName))
                        : ChatColor.translateAlternateColorCodes('&', displayName);
                meta.setDisplayName(finalName);

                // Añadir lore de rareza
                if (rarity != null && rarity.getLoreTag() != null && !rarity.getLoreTag().isEmpty()) {
                    lore.add(0, ChatColor.translateAlternateColorCodes('&', rarity.getLoreTag()));
                }

                meta.setLore(lore);

                // Aplicar encantamientos desde YAML
                if (config.isConfigurationSection("enchantments")) {
                    for (String enchKey : config.getConfigurationSection("enchantments").getKeys(false)) {
                        try {
                            Enchantment ench = Enchantment.getByName(enchKey.toUpperCase());
                            int level = config.getInt("enchantments." + enchKey);
                            if (ench != null) {
                                meta.addEnchant(ench, level, true);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }

                // Marcar con PersistentDataContainer para identificarlo
                NamespacedKey idKey = new NamespacedKey(plugin, "item_id");
                meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, key);

                itemStack.setItemMeta(meta);

                CustomItem customItem = new CustomItem(key, itemStack, config);

// Leer sección de efectos del YAML
                if (config.isConfigurationSection("effects")) {
                    ConfigurationSection effectsSection = config.getConfigurationSection("effects");

                    for (String trigger : effectsSection.getKeys(false)) {
                        ConfigurationSection triggerSection = effectsSection.getConfigurationSection(trigger);
                        List<Effect> parsedEffects = EffectFactory.parseEffects(triggerSection);

                        customItem.setEffectsForTrigger(trigger, parsedEffects);
                    }
                }

                customItems.put(key, customItem);


            } catch (Exception e) {
                plugin.getLogger().warning("Error al cargar item desde " + file.getName() + ": " + e.getMessage());
            }
        }

        plugin.getLogger().info("Cargados " + customItems.size() + " ítems personalizados.");
    }

    public CustomItem getCustomItemByKey(String key) {
        return customItems.get(key);
    }

    public Collection<CustomItem> getAllCustomItems() {
        return customItems.values();
    }

    public int getItemCount() {
        return customItems.size();
    }

    public CustomItem getCustomItemByItemStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;

        ItemMeta meta = itemStack.getItemMeta();
        NamespacedKey idKey = new NamespacedKey(plugin, "item_id");

        if (meta != null && meta.getPersistentDataContainer().has(idKey, PersistentDataType.STRING)) {
            String key = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
            return customItems.get(key);
        }

        return null;
    }
}
