package bw.development.facelessItems.Items;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.Effects.Effect;
import bw.development.facelessItems.Effects.EffectFactory;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Rarity.Rarity;
import bw.development.facelessItems.Rarity.RarityManager;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.registry.GlobalRegistry;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.io.File;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Level;

public class CustomItemManager {

    private final FacelessItems plugin;
    private final Map<String, CustomItem> customItems = new HashMap<>();
    private final AuraSkillsManager auraSkillsManager;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.##");

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

        ConfigurationSection loreSettings = plugin.getConfig().getConfigurationSection("lore-settings");
        List<String> loreOrder = loreSettings != null ? loreSettings.getStringList("order") : new ArrayList<>();
        ConfigurationSection auraSkillsLoreConfig = loreSettings != null ? loreSettings.getConfigurationSection("auraskills-lore") : null;

        boolean displayStatsHeader = auraSkillsLoreConfig != null ? auraSkillsLoreConfig.getBoolean("display_header", true) : true;
        String statsHeaderText = auraSkillsLoreConfig != null ? auraSkillsLoreConfig.getString("header_text", "&a--- Estadísticas ---") : "&a--- Estadísticas ---";
        String statFormat = auraSkillsLoreConfig != null ? auraSkillsLoreConfig.getString("stat_format", "{color}{symbol} &r&a{amount} {name}") : "{color}{symbol} &r&a{amount} {name}";

        MiniMessage miniMessage = MiniMessage.miniMessage();
        LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();
        PlainTextComponentSerializer plain = PlainTextComponentSerializer.plainText();

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

                if (meta instanceof PotionMeta potionMeta && config.isConfigurationSection("potion-meta")) {

                    ConfigurationSection metaSection = config.getConfigurationSection("potion-meta");

                    // La lógica para aplicar color y efectos es la misma para ambos.
                    if (metaSection.isString("color")) {
                        String hexColor = metaSection.getString("color").replace("#", "");
                        try {
                            java.awt.Color awtColor = java.awt.Color.decode("0x" + hexColor);
                            potionMeta.setColor(org.bukkit.Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Color hexadecimal inválido '" + hexColor + "' en el ítem " + key);
                        }
                    }
                    if (metaSection.isList("custom-effects")) {
                        List<Map<?, ?>> effectsList = metaSection.getMapList("custom-effects");
                        for (Map<?, ?> effectMap : effectsList) {
                            String typeName = (String) effectMap.get("type");
                            PotionEffectType effectType = PotionEffectType.getByName(typeName.toUpperCase());
                            if (effectType != null) {
                                int duration = EffectFactory.getSafeInt(effectMap.get("duration"), 200);
                                int amplifier = EffectFactory.getSafeInt(effectMap.get("amplifier"), 0);
                                potionMeta.addCustomEffect(new org.bukkit.potion.PotionEffect(effectType, duration, amplifier), true);
                            }
                        }
                    }
                }

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

                List<String> finalLore = new ArrayList<>();
                AuraSkillsApi auraSkillsApi = AuraSkillsApi.get();
                GlobalRegistry registry = (auraSkillsApi != null) ? auraSkillsApi.getGlobalRegistry() : null;

                List<String> rarityLore = new ArrayList<>();
                if (rarity != null && rarity.getLoreTag() != null && !rarity.getLoreTag().isEmpty()) {
                    rarityLore.add(ChatColor.translateAlternateColorCodes('&', rarity.getLoreTag()));
                }

                List<String> statsLore = new ArrayList<>();
                if (registry != null && !auraSkillsStats.isEmpty()) {
                    if (displayStatsHeader) {
                        statsLore.add(ChatColor.translateAlternateColorCodes('&', statsHeaderText));
                    }
                    for (Map<String, Object> statBoost : auraSkillsStats) {
                        String statName = (String) statBoost.get("stat");
                        Object amountObj = statBoost.get("amount");
                        Stat stat = registry.getStat(NamespacedId.of("auraskills", statName.toLowerCase()));
                        if (stat != null && amountObj instanceof Number) {
                            String sign = (((Number) amountObj).doubleValue() >= 0) ? "+" : "";
                            String formattedAmount = formatNumber(((Number) amountObj).doubleValue());

                            String statColor = miniToLegacyColor(stat.getColor(Locale.getDefault()));
                            String rawSymbol = stripAllFormatting(stat.getSymbol(Locale.getDefault()));

                            String displayNameTranslated = stat.getDisplayName(Locale.forLanguageTag("es"));

                            // Reemplazos en el formato
                            String line = statFormat
                                    .replace("{color}", statColor)
                                    .replace("{symbol}", rawSymbol)
                                    .replace("{amount}", sign + formattedAmount)
                                    .replace("{name}", displayNameTranslated);

                            statsLore.add(ChatColor.translateAlternateColorCodes('&', line));
                        }
                    }
                }

                for (String placeholder : loreOrder) {
                    if (placeholder.equals("{rarity}")) {
                        finalLore.addAll(rarityLore);
                    } else if (placeholder.equals("{original_lore}")) {
                        finalLore.addAll(originalLore);
                    } else if (placeholder.equals("{auraskills_stats}")) {
                        finalLore.addAll(statsLore);
                    } else {
                        finalLore.add(ChatColor.translateAlternateColorCodes('&', placeholder));
                    }
                }

                meta.setLore(finalLore);

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
                        for (String flagName : hideFlags) {
                            try {
                                ItemFlag flag = ItemFlag.valueOf(flagName.toUpperCase());
                                meta.addItemFlags(flag);
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
                // 1. Leemos las propiedades específicas
                int customExperience = -1;
                if (material == Material.EXPERIENCE_BOTTLE && config.isConfigurationSection("xp-bottle-meta")) {
                    customExperience = config.getInt("xp-bottle-meta.experience", -1);
                }


                ItemStack finalItem = auraSkillsManager.applyStatsToItem(itemStack, auraSkillsStats);

                List<BaseEffect> passiveEffects = new ArrayList<>();

                // CORRECCIÓN CRÍTICA: La clave "passive_effects" existe y la cargamos.
                if (config.contains("passive_effects")) {
                    Object rawPassiveData = config.get("passive_effects");

                    // Usamos el parseador de triggers universal para cargar los efectos de la lista/sección.
                    passiveEffects = EffectFactory.parseTriggerEffects(rawPassiveData).stream()
                            .filter(BaseEffect.class::isInstance)
                            .map(BaseEffect.class::cast)
                            .collect(Collectors.toList());
                }

                CustomItem customItem = new CustomItem(key, finalItem, config, auraSkillsStats, customExperience, passiveEffects);

                // 4. Cargamos los efectos del plugin
                if (config.isConfigurationSection("effects")) {
                    ConfigurationSection effectsSection = config.getConfigurationSection("effects");
                    for (String trigger : effectsSection.getKeys(false)) {
                        Object raw = effectsSection.get(trigger);
                        List<Effect> parsedEffects = EffectFactory.parseTriggerEffects(raw);
                        if (!parsedEffects.isEmpty()) {
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


    private String formatNumber(double number) {
        if (number == Math.floor(number)) {
            return String.valueOf((int) number);
        }
        return decimalFormat.format(number);
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

    // Convierte tags de MiniMessage (<red>) a códigos legacy (&c)
    private String miniToLegacyColor(String mini) {
        if (mini == null) return "";
        switch (mini.toLowerCase(Locale.ROOT)) {
            case "<red>": return "&c";
            case "<green>": return "&a";
            case "<blue>": return "&9";
            case "<yellow>": return "&e";
            case "<dark_purple>": return "&5";
            case "<light_purple>": return "&d";
            case "<aqua>": return "&b";
            case "<dark_aqua>": return "&3";
            case "<gold>": return "&6";
            case "<gray>": return "&7";
            case "<dark_gray>": return "&8";
            case "<black>": return "&0";
            case "<white>": return "&f";
            default: return "";
        }
    }

    // Limpia cualquier formato (&c, &o, <red>, etc.) y deja solo el texto plano
    private String stripAllFormatting(String input) {
        if (input == null) return "";
        // quita tags de MiniMessage como <red>, <italic>
        String noMiniMessage = input.replaceAll("<.*?>", "");
        // quita códigos de color Bukkit (&a, &5, etc.)
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', noMiniMessage));
    }

    // CustomItemManager.java

    /**
     * Remueve una cantidad específica de un ítem custom del inventario.
     * @param inventory El inventario a modificar.
     * @param key La clave (ID) del ítem custom a remover (ej: "pico_dragon").
     * @param amount La cantidad a remover.
     * @return true si se removió la cantidad solicitada (o más), false si no se tenía suficiente.
     */
    public boolean takeItemFromInventory(org.bukkit.inventory.Inventory inventory, String key, int amount) {
        if (amount <= 0) return true; // Si la cantidad es 0 o menos, ya está "removido"

        // Primero, verificamos si hay suficiente cantidad del ítem custom en el inventario.
        int foundAmount = 0;

        // Necesitamos la NamespacedKey que usaste para el item_id:
        NamespacedKey idKey = new NamespacedKey(plugin, "item_id");

        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack != null && itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta.getPersistentDataContainer().has(idKey, PersistentDataType.STRING)) {
                    String itemId = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);

                    // Si encontramos el ID correcto, sumamos la cantidad.
                    if (key.equals(itemId)) {
                        foundAmount += itemStack.getAmount();
                    }
                }
            }
        }

        // Si no hay suficiente, fallamos.
        if (foundAmount < amount) {
            return false;
        }

        // Si hay suficiente, procedemos a remover la cantidad exacta.
        return removeCustomItemStacks(inventory, key, amount);
    }

    /**
     * Lógica interna para remover ítems custom por slots.
     * Debe ser llamado SOLAMENTE después de verificar que la cantidad existe (por takeItemFromInventory).
     */
    private boolean removeCustomItemStacks(org.bukkit.inventory.Inventory inventory, String key, int amount) {
        int toRemove = amount;
        NamespacedKey idKey = new NamespacedKey(plugin, "item_id");

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);

            if (itemStack != null && itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta.getPersistentDataContainer().has(idKey, PersistentDataType.STRING)) {
                    String itemId = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);

                    // Si encontramos el ítem custom correcto
                    if (key.equals(itemId)) {
                        int stackAmount = itemStack.getAmount();

                        if (stackAmount <= toRemove) {
                            // Si el stack es menor o igual a lo que necesitamos remover, eliminamos todo el stack
                            toRemove -= stackAmount;
                            inventory.setItem(i, null);
                        } else {
                            // Si el stack es mayor, simplemente reducimos la cantidad
                            itemStack.setAmount(stackAmount - toRemove);
                            toRemove = 0;
                            // No necesitamos seguir iterando
                        }

                        if (toRemove == 0) {
                            break;
                        }
                    }
                }
            }
        }

        // Si llegamos aquí, toRemove debe ser 0 porque ya verificamos que había suficiente cantidad
        return toRemove == 0;
    }

    /**
     * Obtiene solo la clave (String ID) de un ItemStack usando la PersistentDataContainer.
     * @param itemStack El ítem a verificar.
     * @return La clave del ítem custom, o null.
     */
    @Nullable
    public String getCustomItemKeyFromItemStack(ItemStack itemStack) {
        if (itemStack == null || !itemStack.hasItemMeta()) return null;

        ItemMeta meta = itemStack.getItemMeta();
        NamespacedKey idKey = new NamespacedKey(plugin, "item_id"); // Usamos la clave de tu sistema

        if (meta != null && meta.getPersistentDataContainer().has(idKey, PersistentDataType.STRING)) {
            // Lee y devuelve el ID almacenado en el meta.
            return meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);
        }
        return null;
    }

}