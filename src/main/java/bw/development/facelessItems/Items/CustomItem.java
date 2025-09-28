package bw.development.facelessItems.Items;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.Effects.Effect;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration; // Añadido por si acaso
import org.bukkit.inventory.ItemStack;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItem {

    private final String key;
    private final ItemStack itemStack;
    private final FileConfiguration config;
    private final Map<String, List<Effect>> effectsByTrigger = new HashMap<>();
    private final List<BaseEffect> passiveEffects;
    private final List<Map<String, Object>> auraSkillsStats;
    private final int customExperience;

    public CustomItem(String key, ItemStack itemStack, FileConfiguration config, List<Map<String, Object>> auraSkillsStats, int customExperience, List<BaseEffect> passiveEffects) {
        this.key = key;
        this.itemStack = itemStack;
        this.config = config;
        this.auraSkillsStats = auraSkillsStats;
        this.customExperience = customExperience;
        this.passiveEffects = passiveEffects;
    }

    public String getKey() {
        return key;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void setEffectsForTrigger(String trigger, List<Effect> effects) {
        effectsByTrigger.put(trigger, effects);
    }

    public List<Effect> getEffects(String trigger) {
        return effectsByTrigger.getOrDefault(trigger, Collections.emptyList());
    }

    // --- MÉTODO GETTER AÑADIDO ---
    public List<BaseEffect> getPassiveEffects() {
        return passiveEffects;
    }

    public List<Map<String, Object>> getAuraSkillsStats() {
        return auraSkillsStats;
    }

    public int getCustomExperience() {
        return customExperience;
    }
}