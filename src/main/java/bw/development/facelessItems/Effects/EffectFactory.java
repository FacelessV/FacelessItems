package bw.development.facelessItems.Effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EffectFactory {

    // Ahora parsea directamente desde la sección del trigger (ej: effects.on_hit)
    public static List<Effect> parseEffects(ConfigurationSection triggerSection) {
        List<Effect> effects = new ArrayList<>();
        if (triggerSection == null) return effects;

        for (String key : triggerSection.getKeys(false)) {
            ConfigurationSection effectSection = triggerSection.getConfigurationSection(key);
            if (effectSection != null) {
                Effect effect = createEffect(effectSection);
                if (effect != null) {
                    effects.add(effect);
                }
            }
        }

        return effects;
    }

    public static Effect createEffect(ConfigurationSection section) {
        String type = section.getString("type", "").toUpperCase();
        String targetStr = section.getString("target", "PLAYER").toUpperCase();

        EffectTarget target;
        try {
            target = EffectTarget.valueOf(targetStr);
        } catch (IllegalArgumentException e) {
            target = EffectTarget.PLAYER;
        }

        return switch (type) {
            case "POTION" -> {
                String potionName = section.getString("potion-type", "REGENERATION").toUpperCase();
                PotionEffectType potionType = PotionEffectType.getByName(potionName);
                int duration = section.getInt("duration", 60);
                int amplifier = section.getInt("amplifier", 0);
                yield (potionType != null)
                        ? new PotionEffectCustom(potionType, duration, amplifier, target)
                        : null;
            }
            case "DAMAGE" -> {
                double amount = section.getDouble("amount", 5.0);
                yield new DamageEffect(amount, target);
            }
            case "HEAL" -> {
                double amount = section.getDouble("amount", 5.0);
                yield new HealEffect(amount, target);
            }
            case "MESSAGE" -> {
                String text = section.getString("text", "Mensaje vacío");
                yield new MessageEffect(text);
            }
            default -> null;
        };
    }
}
