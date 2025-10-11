package bw.development.facelessItems.Items;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.Effects.Effect;
import bw.development.facelessItems.Effects.EffectFactory;
import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Rarity.Rarity;
import bw.development.facelessItems.Rarity.RarityManager;
import com.destroystokyo.paper.profile.PlayerProfile;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.registry.GlobalRegistry;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;

import javax.annotation.Nullable;
import java.io.File;
import java.net.URL;
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

                // --- AÑADIR: LÓGICA DE TEXTURA BASE64 ---
                String base64Texture = config.getString("base64_texture");

                if (base64Texture != null && !base64Texture.isEmpty()) {
                    // 1. Forzar el material a ser una cabeza si no lo es (necesario para SkullMeta)
                    if (material != Material.PLAYER_HEAD) {
                        itemStack.setType(Material.PLAYER_HEAD);
                        meta = itemStack.getItemMeta(); // Re-obtener meta por si el tipo cambió
                        if (meta == null) continue;
                    }

                    // 2. Aplicar la textura si es SkullMeta
                    if (meta instanceof org.bukkit.inventory.meta.SkullMeta skullMeta) {
                        try {
                            // Utilizamos la clase utilitaria para aplicar la textura
                            applyTextureToSkullMeta(skullMeta, base64Texture);
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error al aplicar textura Base64 para el ítem " + key + ": " + e.getMessage());
                        }
                    }
                }

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

                if (meta instanceof org.bukkit.inventory.meta.ShieldMeta shieldMeta) {
                    ConfigurationSection designSection = config.getConfigurationSection("properties.shield_design");

                    if (designSection != null) {
                        // 1. Aplicar el Color Base
                        String colorStr = designSection.getString("base_color");
                        if (colorStr != null) {
                            try {
                                org.bukkit.DyeColor baseColor = org.bukkit.DyeColor.valueOf(colorStr.toUpperCase());
                                shieldMeta.setBaseColor(baseColor);
                            } catch (IllegalArgumentException e) {
                                plugin.getLogger().warning("Color base inválido para el escudo: " + colorStr);
                            }
                        }

                        // 2. Aplicar los Patrones (Heredado de BannerMeta)
                        if (designSection.isList("patterns")) {
                            List<Map<?, ?>> patternsList = designSection.getMapList("patterns");

                            // Limpiamos los patrones existentes antes de aplicar los nuevos
                            shieldMeta.setPatterns(new java.util.ArrayList<>());

                            for (Map<?, ?> patternMap : patternsList) {
                                String patColorStr = (String) patternMap.get("color");
                                String patTypeStr = (String) patternMap.get("type");

                                try {
                                    org.bukkit.DyeColor patColor = org.bukkit.DyeColor.valueOf(patColorStr.toUpperCase());
                                    org.bukkit.block.banner.PatternType patType = org.bukkit.block.banner.PatternType.valueOf(patTypeStr.toUpperCase());

                                    // Creamos el patrón y lo añadimos
                                    shieldMeta.addPattern(new org.bukkit.block.banner.Pattern(patColor, patType));

                                } catch (IllegalArgumentException e) {
                                    plugin.getLogger().warning("Patrón o color inválido en el diseño del escudo: " + patTypeStr);
                                }
                            }
                        }
                    }
                }

                if (meta instanceof Damageable damageable) {
                    ConfigurationSection propertiesSection = config.getConfigurationSection("properties");

                    if (propertiesSection != null) {

                        // 1. Establecer la durabilidad MÁXIMA (max_durability)
                        if (propertiesSection.contains("max_durability")) {
                            int maxDurability = propertiesSection.getInt("max_durability");
                            // Usamos setMaxDamage() para establecer el valor máximo de durabilidad
                            damageable.setMaxDamage(maxDurability);
                        }

                        // 2. Establecer el daño INICIAL (cuánto está gastado)
                        if (propertiesSection.contains("initial_damage")) {
                            int initialDamage = propertiesSection.getInt("initial_damage");
                            // Usamos setDamage() para establecer cuánto daño tiene (0 = nuevo)
                            // Nos aseguramos de que no exceda el máximo
                            damageable.setDamage(Math.min(initialDamage, damageable.getMaxDamage()));
                        }
                    }
                }

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

                ConfigurationSection attrSection = config.getConfigurationSection("attributes");

                if (attrSection != null) {
                    // 1. Iterar sobre CADA ATRIBUTO (Ej: ARMOR, MAX_HEALTH)
                    for (String attrName : attrSection.getKeys(false)) {

                        ConfigurationSection attributeMods = attrSection.getConfigurationSection(attrName);
                        if (attributeMods == null) continue;

                        try { // <- TRY 1
                            // 🚨 CORRECCIÓN: Usar el sistema de Registros para obtener el atributo
                            Attribute attribute = Bukkit.getRegistry(Attribute.class).get(NamespacedKey.minecraft(attrName.toLowerCase()));

                            if (attribute == null) {
                                // Si el registro devuelve null, lanzamos la excepción para que el CATCH 1 la capture.
                                throw new IllegalArgumentException("Atributo no encontrado en el Registro.");
                            }

                            // 2. Iterar sobre CADA MODIFICADOR DENTRO del atributo
                            for (String modId : attributeMods.getKeys(false)) {
                                ConfigurationSection modSection = attributeMods.getConfigurationSection(modId);
                                if (modSection == null) continue;

                                try { // <- TRY 2: Para atrapar los errores en la conversión del MODIFICADOR
                                    // Obtener Parámetros del Modificador
                                    double amount = modSection.getDouble("amount");

                                    String opStr = modSection.getString("operation", "ADD_NUMBER");
                                    AttributeModifier.Operation operation =
                                            AttributeModifier.Operation.valueOf(opStr.toUpperCase());

                                    String slotStr = modSection.getString("slot", "ANY");
                                    EquipmentSlotGroup slotGroup = EquipmentSlotGroup.getByName(slotStr.toUpperCase());

                                    // Crear NamespacedKey único
                                    NamespacedKey modifierKey = new NamespacedKey(plugin, key + "_" + attrName.toLowerCase() + "_" + modId);

                                    AttributeModifier modifier =
                                            new AttributeModifier(
                                                    modifierKey,
                                                    amount,
                                                    operation,
                                                    slotGroup
                                            );

                                    // 3. Aplicar el modificador al ItemMeta
                                    meta.addAttributeModifier(attribute, modifier);

                                } catch (IllegalArgumentException e) {
                                    // CATCH 2: Captura errores de conversión en OPERATION, SLOT o AMOUNT.
                                    // Ahora modId y attrName están en el alcance (scope) de este catch.
                                    plugin.getLogger().warning("Error en la configuración del modificador '" + attrName + "." + modId + "' en el ítem " + key + ". Verifique OPERATION o SLOT.");
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            // CATCH 1: Captura errores de conversión en el nombre del ATRIBUTO (Ej: 'VIDA' en lugar de 'MAX_HEALTH')
                            plugin.getLogger().warning("Atributo inválido '" + attrName + "' en item " + key + ". Será omitido.");
                        }
                    }
                }

                ConfigurationSection propertiesSection = config.getConfigurationSection("properties");
                if (propertiesSection != null) {
                    // --- LÓGICA AÑADIDA PARA CUSTOM_MODEL_DATA ---
                    if (propertiesSection.contains("custom_model_data")) {
                        int cmd = propertiesSection.getInt("custom_model_data");
                        meta.setCustomModelData(cmd);
                        plugin.getLogger().log(Level.FINE, "Aplicado CustomModelData " + cmd + " al item " + key);
                    }

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

                ConfigurationSection attrLoreConfig = plugin.getConfig().getConfigurationSection("lore-settings.attributes-lore");
                List<String> bukkitAttrLore = new ArrayList<>();

                boolean isFeatureEnabled = attrLoreConfig != null && attrLoreConfig.getBoolean("enabled", true);

                if (isFeatureEnabled && meta.hasAttributeModifiers()) {

                    // --- INICIALIZACIÓN DEL MAPA DE LOCALIZACIÓN (Más Robusto) ---
                    Map<String, String> localizedNames = new HashMap<>();
                    ConfigurationSection localizedSection = plugin.getConfig().getConfigurationSection("lore-settings.attributes-lore.localized_names");

                    if (localizedSection != null) {
                        // Usa el método getString() para obtener valores y evitar fallos de typesafety
                        for (String attrKey : localizedSection.getKeys(false)) {
                            String localizedName = localizedSection.getString(attrKey);
                            if (localizedName != null) {
                                localizedNames.put(attrKey.toUpperCase(), ChatColor.translateAlternateColorCodes('&', localizedName));
                            }
                        }
                    }
                    // -----------------------------------------------------------

                    // Lectura del formato y cabecera
                    String attrHeaderText = attrLoreConfig.getString("header_text", "&d--- Atributos de Combate ---");
                    String attrFormat = attrLoreConfig.getString("attribute_format", " &6▪ &r&f{amount} &7{name}");

                    Map<Attribute, Double> totalAttributeBonuses = new HashMap<>();

                    // 1. AGREGACIÓN
                    for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : meta.getAttributeModifiers().asMap().entrySet()) {
                        double totalBonus = 0.0;
                        for (AttributeModifier modifier : entry.getValue()) {
                            if (modifier.getOperation() == AttributeModifier.Operation.ADD_NUMBER) {
                                totalBonus += modifier.getAmount();
                            }
                        }
                        if (totalBonus != 0.0) {
                            totalAttributeBonuses.put(entry.getKey(), totalBonus);
                        }
                    }


                    // 2. CONSTRUCCIÓN DEL LORE
                    if (!totalAttributeBonuses.isEmpty()) {
                        if (attrLoreConfig.getBoolean("display_header", true)) {
                            bukkitAttrLore.add(ChatColor.translateAlternateColorCodes('&', attrHeaderText));
                        }

                        for (Map.Entry<Attribute, Double> entry : totalAttributeBonuses.entrySet()) {
                            Attribute attr = entry.getKey();
                            double amount = entry.getValue();

                            String sign = (amount >= 0) ? "+" : "";
                            String formattedAmount = formatNumber(amount);

                            String attrName = getLocalizedAttributeName(attr, localizedNames);

                            String line = attrFormat
                                    .replace("{amount}", sign + formattedAmount)
                                    .replace("{name}", attrName);

                            bukkitAttrLore.add(ChatColor.translateAlternateColorCodes('&', line));
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
                    } else if (placeholder.equals("{attributes-lore}")) { // <--- NUEVO PLACEHOLDER
                        finalLore.addAll(bukkitAttrLore);
                    } else {
                        finalLore.add(ChatColor.translateAlternateColorCodes('&', placeholder));
                    }
                }

                meta.setLore(finalLore);

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

    /**
     * Remueve una cantidad específica de un ítem custom o vanilla del inventario.
     *
     * @param inventory El inventario a modificar.
     * @param materialKey La clave del material (ej: "DIAMOND" o "facelessitems:gema_de_energia").
     * @param amount La cantidad a remover.
     * @return true si se removió la cantidad solicitada, false si no se tenía suficiente.
     */
    public boolean takeItemFromInventory(org.bukkit.inventory.Inventory inventory, String materialKey, int amount) {
        if (amount <= 0) return true;

        // --- 1. PREPARACIÓN DE CLAVES ---
        String customItemId = materialKey.contains(":")
                ? materialKey.substring(materialKey.indexOf(":") + 1)
                : materialKey;

        Material vanillaMaterial = Material.matchMaterial(materialKey);
        NamespacedKey idKey = new NamespacedKey(plugin, "item_id");

        // --- 2. PRIMERA PASADA: CONTEO (Igual que en countItemInInventory) ---
        int foundAmount = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) continue;

            // Chequeo Vanilla
            if (vanillaMaterial != null && itemStack.getType() == vanillaMaterial) {
                foundAmount += itemStack.getAmount();
                continue;
            }

            // Chequeo Custom Item
            if (itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta.getPersistentDataContainer().has(idKey, PersistentDataType.STRING)) {
                    String storedItemId = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);

                    if (customItemId.equals(storedItemId)) {
                        foundAmount += itemStack.getAmount();
                    }
                }
            }
        }

        if (foundAmount < amount) {
            return false;
        }

        // --- 3. SEGUNDA PASADA: REMOCIÓN ---
        if (vanillaMaterial != null) {
            // Si es un ítem Vanilla, usamos el método remove() de Bukkit, que es eficiente.
            // NOTE: El ItemStack con el amount a remover NO tiene que tener metadata.
            ItemStack toRemove = new ItemStack(vanillaMaterial, amount);

            // remove() de Bukkit ignora la metadata y solo se enfoca en el Material
            HashMap<Integer, ItemStack> remaining = inventory.removeItem(toRemove);

            // Si remaining está vacío, significa que todo fue removido.
            return remaining.isEmpty();
        } else {
            // Si NO es un ítem Vanilla (es Custom), usamos tu método auxiliar.
            return removeCustomItemStacks(inventory, customItemId, amount);
        }
    }

    public int countItemInInventory(Player user, String materialKey) { // Cambiamos itemKey a materialKey para claridad
        if (user == null || materialKey == null || materialKey.isEmpty()) {
            return 0;
        }

        int count = 0;

        // --- 1. PREPARACIÓN DE CLAVES ---

        // Extraer solo el ID si hay un namespace (ej: toma "ejemplo1" de "facelessitems:ejemplo1")
        String customItemId = materialKey.contains(":")
                ? materialKey.substring(materialKey.indexOf(":") + 1)
                : materialKey;

        // Intentar resolver la clave como un material Vanilla (Ej: "DIAMOND")
        Material vanillaMaterial = Material.matchMaterial(materialKey);

        // NamespacedKey para Custom Item lookup
        NamespacedKey idKey = new NamespacedKey(plugin, "item_id");

        for (ItemStack itemStack : user.getInventory().getContents()) {
            if (itemStack == null) continue;

            // --- 2. CHEQUEO DE ÍTEM VANILLA ---
            if (vanillaMaterial != null && itemStack.getType() == vanillaMaterial) {
                count += itemStack.getAmount();
                continue;
            }

            // --- 3. CHEQUEO DE CUSTOM ITEM ---
            // Chequeamos solo si el ítemStack tiene metadata (que es donde está nuestro tag)
            if (itemStack.hasItemMeta()) {
                ItemMeta meta = itemStack.getItemMeta();
                if (meta.getPersistentDataContainer().has(idKey, PersistentDataType.STRING)) {
                    String storedItemId = meta.getPersistentDataContainer().get(idKey, PersistentDataType.STRING);

                    // Comparamos el ID almacenado con la clave extraída
                    if (customItemId.equals(storedItemId)) {
                        count += itemStack.getAmount();
                    }
                }
            }
        }

        return count;
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

    /**
     * Aplica una textura Base64 a un SkullMeta utilizando la API de Bukkit PlayerProfile.
     * Esto es compatible con Spigot 1.21 y versiones recientes.
     */
    private void applyTextureToSkullMeta(SkullMeta skullMeta, String base64Texture) throws Exception {
        if (base64Texture == null || base64Texture.isEmpty()) {
            return;
        }

        // 1. Decodificar el Base64 para obtener el JSON completo
        String decodedJson = new String(Base64.getDecoder().decode(base64Texture));

        // 2. Extraer la URL de la Textura
        // ESTA PARTE REQUIERE TU IMPLEMENTACIÓN DE PARSEO JSON
        String textureUrl;
        try {
            // --- INICIO: LÓGICA DE PARSEO JSON (Reemplaza con tu libreria) ---
            // Ejemplo conceptual usando una sintaxis similar a Gson/org.json:
            // El JSON decodificado tiene el formato: {"textures":{"SKIN":{"url":"URL_AQUI"}}}

            // Obtenemos el objeto raíz
            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(decodedJson).getAsJsonObject();

            // Navegamos al campo 'textures' -> 'SKIN' -> 'url'
            textureUrl = jsonObject.getAsJsonObject("textures")
                    .getAsJsonObject("SKIN")
                    .get("url").getAsString();
            // --- FIN: LÓGICA DE PARSEO JSON ---

        } catch (Exception e) {
            // Loguear el JSON para ver qué está mal
            plugin.getLogger().log(Level.WARNING, "Fallo al parsear JSON de textura para Base64: " + decodedJson, e);
            throw new IllegalArgumentException("No se pudo extraer la URL de la textura del JSON decodificado.");
        }

        // 3. Crear el PlayerProfile con un UUID y nombre temporales
        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);

        // 4. Crear el objeto de Texturas y establecer la URL
        PlayerTextures textures = profile.getTextures();

        // Necesitamos la URL como objeto java.net.URL
        URL urlObject = new URL(textureUrl);

        // 5. Asignar la textura de la piel
        textures.setSkin(urlObject);

        // 6. Asignar las texturas y el perfil al SkullMeta
        profile.setTextures(textures);
        skullMeta.setOwnerProfile(profile);
    }

    /**
     * Convierte el nombre técnico del atributo (MAX_HEALTH) a un nombre legible,
     * usando el mapa de localización de la configuración.
     */
    private String getLocalizedAttributeName(Attribute attribute, Map<String, String> localizedNames) {
        String attrKey = attribute.name(); // Ej: "MAX_HEALTH"

        // 1. Buscar en el mapa de localización.
        if (localizedNames.containsKey(attrKey)) {
            return localizedNames.get(attrKey);
        }

        // 2. Fallback: Si no se encuentra, usar el nombre técnico limpio.
        String cleanName = attrKey.replace("_", " ").toLowerCase();
        return cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1);
    }
}