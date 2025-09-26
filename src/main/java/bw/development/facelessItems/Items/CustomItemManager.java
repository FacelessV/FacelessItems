package bw.development.facelessItems.Items;

import bw.development.facelessItems.Effects.Effect;
import bw.development.facelessItems.Effects.EffectFactory;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Rarity.Rarity;
import bw.development.facelessItems.Rarity.RarityManager;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.registry.GlobalRegistry;
import dev.aurelium.auraskills.api.registry.NamespacedId;
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
    private final AuraSkillsManager auraSkillsManager;

    public CustomItemManager(FacelessItems plugin) {
        this.plugin = plugin;
        this.auraSkillsManager = new AuraSkillsManager();
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

        // Leer la configuración global del lore desde config.yml
        List<String> globalLoreConfig = plugin.getConfig().getStringList("lore-settings.global-lore");

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

                List<String> originalLore = config.getStringList("lore").stream()
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

                List<Map<String, Object>> auraSkillsStats = new ArrayList<>();
                if (config.isList("auraskills.stats")) {
                    auraSkillsStats = (List<Map<String, Object>>) config.getList("auraskills.stats");
                }

                // --- NUEVA LÓGICA: CONSTRUIR EL LORE GLOBALMENTE ---
                List<String> finalLore = new ArrayList<>();
                AuraSkillsApi auraSkillsApi = AuraSkillsApi.get();
                GlobalRegistry registry = (auraSkillsApi != null) ? auraSkillsApi.getGlobalRegistry() : null;

                // Lore de la rareza
                String rarityLore = (rarity != null && rarity.getLoreTag() != null && !rarity.getLoreTag().isEmpty())
                        ? ChatColor.translateAlternateColorCodes('&', rarity.getLoreTag()) : "";

                // Lore de las estadísticas
                List<String> statsLore = new ArrayList<>();
                if (registry != null && !auraSkillsStats.isEmpty()) {
                    for (Map<String, Object> statBoost : auraSkillsStats) {
                        String statName = (String) statBoost.get("stat");
                        Object amountObj = statBoost.get("amount");
                        Stat stat = registry.getStat(NamespacedId.of("auraskills", statName.toLowerCase()));
                        if (stat != null && amountObj instanceof Number) {
                            String sign = (((Number) amountObj).doubleValue() >= 0) ? "+" : "";
                            String statLine = ChatColor.translateAlternateColorCodes('&', "&7" + sign + ((Number) amountObj).doubleValue() + " " + stat.getDisplayName(Locale.getDefault()));
                            statsLore.add(statLine);
                        }
                    }
                }

                // Construir el lore final
                for (String line : globalLoreConfig) {
                    String processedLine = line;
                    processedLine = processedLine.replace("{rarity}", rarityLore);
                    processedLine = processedLine.replace("{original-lore}", String.join("\n", originalLore));
                    processedLine = processedLine.replace("{auraskills-stats}", String.join("\n", statsLore));

                    if (processedLine.contains("{original-lore}")) {
                        for (String l : originalLore) finalLore.add(l);
                    } else if (processedLine.contains("{auraskills-stats}")) {
                        for (String l : statsLore) finalLore.add(l);
                    } else {
                        finalLore.add(ChatColor.translateAlternateColorCodes('&', processedLine));
                    }
                }

                meta.setLore(finalLore);
                // --- FIN DE LA LÓGICA DEL LORE ---

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

                ItemStack finalItem = auraSkillsManager.applyStatsToItem(itemStack, auraSkillsStats);

                CustomItem customItem = new CustomItem(key, finalItem, config, auraSkillsStats);

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

    private String formatStatName(String name) {
        String[] parts = name.split("_");
        StringBuilder formattedName = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                formattedName.append(part.substring(0, 1).toUpperCase()).append(part.substring(1).toLowerCase()).append(" ");
            }
        }
        return formattedName.toString().trim();
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