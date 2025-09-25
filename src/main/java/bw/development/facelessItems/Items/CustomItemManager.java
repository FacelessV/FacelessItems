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
import java.util.logging.Level;

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
            String key = file.getName().replace(".yml", "");

            try {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

                String matName = config.getString("material", "STONE").toUpperCase(Locale.ROOT);
                Material material;
                try {
                    material = Material.valueOf(matName);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Material inválido '" + matName + "' en item " + key + ". Usando STONE.");
                    material = Material.STONE;
                }

                String displayName = config.getString("display-name", "Custom Item: " + key);

                List<String> lore = config.getStringList("lore").stream()
                        .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                        .collect(Collectors.toList());

                ItemStack itemStack = new ItemStack(material);
                ItemMeta meta = itemStack.getItemMeta();
                if (meta == null) continue;

                String rarityId = config.getString("rarity", "COMMON").toUpperCase();
                RarityManager rarityManager = plugin.getRarityManager();
                Rarity rarity = rarityManager.getRarity(rarityId);

                if (rarity == null) {
                    plugin.getLogger().warning("Rareza '" + rarityId + "' no encontrada para el ítem " + key + ".");
                }

                String finalName = rarity != null
                        ? ChatColor.translateAlternateColorCodes('&', rarity.getColor() + ChatColor.stripColor(displayName))
                        : ChatColor.translateAlternateColorCodes('&', displayName);
                meta.setDisplayName(finalName);

                if (rarity != null && rarity.getLoreTag() != null && !rarity.getLoreTag().isEmpty()) {
                    lore.add(0, ChatColor.translateAlternateColorCodes('&', rarity.getLoreTag()));
                }

                meta.setLore(lore);

                if (config.isConfigurationSection("enchantments")) {
                    for (String enchKey : config.getConfigurationSection("enchantments").getKeys(false)) {
                        try {
                            Enchantment ench = Enchantment.getByName(enchKey.toUpperCase());
                            int level = config.getInt("enchantments." + enchKey);
                            if (ench != null) {
                                meta.addEnchant(ench, level, true);
                            } else {
                                plugin.getLogger().warning("Encantamiento desconocido: " + enchKey + " en item " + key);
                            }
                        } catch (Exception ignored) {}
                    }
                }

                ConfigurationSection propertiesSection = config.getConfigurationSection("properties");
                if (propertiesSection != null) {
                    List<String> hideFlags = propertiesSection.getStringList("hide-flags");
                    if (!hideFlags.isEmpty()) {
                        plugin.getLogger().info("Ocultando flags para el item " + key + ": " + hideFlags.toString());
                        for (String flagName : hideFlags) {
                            try {
                                ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                                meta.addItemFlags(flag);
                                plugin.getLogger().info("   - Flag " + flag.name() + " oculta con éxito.");
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Flag desconocida en item " + key + ": " + flagName);
                            }
                        }
                    }

                    if (propertiesSection.getBoolean("unbreakable", false)) {
                        meta.setUnbreakable(true);
                    }
                }

                NamespacedKey idKey = new NamespacedKey(plugin, "item_id");
                meta.getPersistentDataContainer().set(idKey, PersistentDataType.STRING, key);
                itemStack.setItemMeta(meta);

                CustomItem customItem = new CustomItem(key, itemStack, config);

                if (config.isConfigurationSection("effects")) {
                    ConfigurationSection effectsSection = config.getConfigurationSection("effects");
                    for (String trigger : effectsSection.getKeys(false)) {
                        Object raw = effectsSection.get(trigger);
                        List<Effect> parsedEffects = EffectFactory.parseTriggerEffects(raw);
                        if (!parsedEffects.isEmpty()) {
                            plugin.getLogger().info("Cargados " + parsedEffects.size() + " efectos para trigger " + trigger + " en item " + key);
                            customItem.setEffectsForTrigger(trigger, parsedEffects);
                        }
                    }
                }
                customItems.put(key, customItem);

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error al cargar ítem desde " + file.getName() + ": " + e.getMessage(), e);
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