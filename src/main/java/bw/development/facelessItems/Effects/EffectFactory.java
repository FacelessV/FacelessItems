package bw.development.facelessItems.Effects;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EffectFactory {

    // Maneja listas (caso de tu YAML con "- type: ...")
    public static Effect createEffect(Map<?, ?> map) {
        if (map == null) return null;

        Object rawType = map.get("type");
        String type = rawType != null ? rawType.toString().toUpperCase() : "";

        Object rawTarget = map.get("target");
        String targetStr = rawTarget != null ? rawTarget.toString().toUpperCase() : "PLAYER";


        EffectTarget target;
        try {
            target = EffectTarget.valueOf(targetStr);
        } catch (IllegalArgumentException e) {
            target = EffectTarget.PLAYER;
        }

        return switch (type) {
            case "HEAL" -> {
                Object rawAmount = map.get("amount");
                double amount = rawAmount != null ? Double.parseDouble(rawAmount.toString()) : 5.0;
                yield new HealEffect(amount, target);
            }
            case "DAMAGE" -> {
                Object rawAmount = map.get("amount");
                double amount = rawAmount != null ? Double.parseDouble(rawAmount.toString()) : 5.0;
                yield new DamageEffect(amount, target);
            }
            case "MESSAGE" -> {
                Object rawText = map.get("text");
                String text = rawText != null ? rawText.toString() : "Mensaje vacío";
                yield new MessageEffect(text);
            }
            default -> null;
        };

    }


    // Maneja subsecciones (por si alguien define efectos como bloques en lugar de listas)
    public static List<Effect> parseEffects(ConfigurationSection section) {
        List<Effect> effects = new ArrayList<>();
        if (section == null) return effects;

        for (String key : section.getKeys(false)) {
            ConfigurationSection effectSection = section.getConfigurationSection(key);
            if (effectSection == null) continue;

            String type = effectSection.getString("type", "").toUpperCase();
            String targetStr = effectSection.getString("target", "PLAYER").toUpperCase();

            EffectTarget target;
            try {
                target = EffectTarget.valueOf(targetStr);
            } catch (IllegalArgumentException e) {
                target = EffectTarget.PLAYER;
            }

            switch (type) {
                case "HEAL" -> {
                    double amount = effectSection.getDouble("amount", 5.0);
                    effects.add(new HealEffect(amount, target));
                }
                case "DAMAGE" -> {
                    double amount = effectSection.getDouble("amount", 5.0);
                    effects.add(new DamageEffect(amount, target));
                }
                case "MESSAGE" -> {
                    String text = effectSection.getString("text", "Mensaje vacío");
                    effects.add(new MessageEffect(text));
                }
            }
        }
        return effects;
    }
}
