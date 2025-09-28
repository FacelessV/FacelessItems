package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.Effects.EffectFactory;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class SetManager {

    private final FacelessItems plugin;
    private final Map<String, ArmorSet> armorSets = new HashMap<>();

    public SetManager(FacelessItems plugin) {
        this.plugin = plugin;
        loadSets();
    }

    public void loadSets() {
        armorSets.clear();
        File setsFile = new File(plugin.getDataFolder(), "sets.yml");
        if (!setsFile.exists()) {
            plugin.saveResource("sets.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(setsFile);
        ConfigurationSection setsSection = config.getConfigurationSection("sets");
        if (setsSection == null) return;

        // Iteramos sobre cada set (ej: "volcanico")
        for (String setKey : setsSection.getKeys(false)) {
            ConfigurationSection setConfig = setsSection.getConfigurationSection(setKey);
            if (setConfig == null) continue;

            String displayName = setConfig.getString("display-name", setKey);
            Set<String> itemKeys = new HashSet<>(setConfig.getStringList("items"));
            Map<Integer, ArmorSetBonus> bonuses = new HashMap<>();

            ConfigurationSection bonusesSection = setConfig.getConfigurationSection("bonuses");
            if (bonusesSection != null) {
                // Iteramos sobre cada bonus por pieza (ej: "2", "4")
                for (String pieceCountStr : bonusesSection.getKeys(false)) {
                    try {
                        int pieceCount = Integer.parseInt(pieceCountStr);
                        ConfigurationSection bonusConfig = bonusesSection.getConfigurationSection(pieceCountStr);
                        if (bonusConfig == null) continue;

                        // Parseamos los efectos pasivos
                        List<BaseEffect> passiveEffects = parseEffectsFromSection(bonusConfig, "passive_effects");

                        // Parseamos los efectos por trigger
                        Map<String, List<BaseEffect>> triggeredEffects = new HashMap<>();
                        if (bonusConfig.isConfigurationSection("triggered_effects")) {
                            ConfigurationSection triggersConfig = bonusConfig.getConfigurationSection("triggered_effects");
                            for (String triggerKey : triggersConfig.getKeys(false)) {
                                List<BaseEffect> effects = parseEffectsFromSection(triggersConfig, triggerKey);
                                triggeredEffects.put(triggerKey, effects);
                            }
                        }

                        bonuses.put(pieceCount, new ArmorSetBonus(passiveEffects, triggeredEffects));
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("La clave de bonus '" + pieceCountStr + "' en el set '" + setKey + "' no es un número válido.");
                    }
                }
            }

            armorSets.put(setKey, new ArmorSet(setKey, displayName, itemKeys, bonuses));
        }
        plugin.getLogger().info("Cargados " + armorSets.size() + " sets de armadura.");
    }

    private List<BaseEffect> parseEffectsFromSection(ConfigurationSection section, String key) {
        Object rawData = section.get(key);
        if (rawData == null) return Collections.emptyList();

        return EffectFactory.parseTriggerEffects(rawData).stream()
                .filter(BaseEffect.class::isInstance)
                .map(BaseEffect.class::cast)
                .collect(Collectors.toList());
    }

    public Collection<ArmorSet> getArmorSets() {
        return armorSets.values();
    }

    /**
     * Busca un ArmorSet específico por su clave (ID).
     * @param key La clave del set (ej: "volcanico").
     * @return El objeto ArmorSet, o null si no se encuentra.
     */
    public ArmorSet getArmorSet(String key) {
        return armorSets.get(key);
    }
}