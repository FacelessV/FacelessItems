package bw.development.facelessItems.Effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

public class EffectFactory {

    private static double getSafeDouble(Object raw, double defaultValue) {
        if (raw == null) return defaultValue;
        try {
            if (raw instanceof Number) {
                return ((Number) raw).doubleValue();
            }
            return Double.parseDouble(raw.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static int getSafeInt(Object raw, int defaultValue) {
        if (raw == null) return defaultValue;
        try {
            if (raw instanceof Number) {
                return ((Number) raw).intValue();
            }
            return Integer.parseInt(raw.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static Effect createEffectFromProperties(String type, Map<String, Object> properties) {
        String upperType = type.toUpperCase(Locale.ROOT);

        String targetStr = (String) properties.getOrDefault("target", "PLAYER");
        EffectTarget target;
        try {
            target = EffectTarget.valueOf(targetStr.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            target = EffectTarget.PLAYER;
        }

        return switch (upperType) {
            case "HEAL" -> {
                double amount = getSafeDouble(properties.get("amount"), 5.0);
                yield new HealEffect(amount, target);
            }
            case "DAMAGE" -> {
                double amount = getSafeDouble(properties.get("amount"), 5.0);
                yield new DamageEffect(amount, target);
            }
            case "MESSAGE" -> {
                String text = (String) properties.getOrDefault("text", "Mensaje vacío");
                yield new MessageEffect(text);
            }
            case "POTION" -> {
                String potionTypeName = (String) properties.getOrDefault("potion_type", "SPEED");
                PotionEffectType potionType = PotionEffectType.getByName(potionTypeName.toUpperCase());
                int duration = getSafeInt(properties.get("duration"), 100);
                int amplifier = getSafeInt(properties.get("amplifier"), 0);

                if (potionType == null) yield null;
                yield new PotionEffect(potionType, duration, amplifier, target);
            }
            case "LIGHTNING" -> {
                yield new LightningEffect(target);
            }
            case "BREAK_BLOCK" -> {
                int radius = getSafeInt(properties.get("radius"), 1);
                int layers = getSafeInt(properties.get("layers"), 1);
                yield new BreakBlockEffect(radius, layers);
            }
            case "VEIN_MINE" -> {
                int maxBlocks = getSafeInt(properties.get("max_blocks"), 64);
                yield new VeinMineEffect(maxBlocks);
            }
            default -> null;
        };
    }

    public static Effect createEffect(Map<?, ?> map) {
        if (map == null) return null;
        Map<String, Object> properties = new HashMap<>();
        map.forEach((k, v) -> properties.put(k.toString(), v));
        String type = (String) properties.getOrDefault("type", "");
        if (type.isEmpty()) return null;
        return createEffectFromProperties(type, properties);
    }

    public static List<Effect> parseEffects(ConfigurationSection section) {
        List<Effect> effects = new ArrayList<>();
        if (section == null) return effects;
        for (String key : section.getKeys(false)) {
            ConfigurationSection effectSection = section.getConfigurationSection(key);
            if (effectSection == null) continue;
            Map<String, Object> properties = effectSection.getValues(true);
            String type = (String) properties.getOrDefault("type", "");
            if (type.isEmpty()) continue;
            Effect effect = createEffectFromProperties(type, properties);
            if (effect != null) {
                effects.add(effect);
            }
        }
        return effects;
    }

    public static List<Effect> parseTriggerEffects(Object rawData) {
        if (rawData == null) {
            return Collections.emptyList();
        }
        if (rawData instanceof List) {
            List<Effect> effects = new ArrayList<>();
            for (Object item : (List<?>) rawData) {
                if (item instanceof Map) {
                    Effect effect = createEffect((Map<?, ?>) item);
                    if (effect != null) {
                        effects.add(effect);
                    }
                }
            }
            return effects;
        }
        else if (rawData instanceof ConfigurationSection) {
            return parseEffects((ConfigurationSection) rawData);
        }
        return Collections.emptyList();
    }
}