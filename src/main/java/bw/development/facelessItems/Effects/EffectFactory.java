package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.*;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.stream.Collectors;

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

    private static List<Material> getSafeMaterialList(Object rawList) {
        List<Material> materials = new ArrayList<>();
        if (rawList instanceof List) {
            for (Object item : (List<?>) rawList) {
                if (item instanceof String) {
                    try {
                        materials.add(Material.valueOf(((String) item).toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        // Puedes logear una advertencia si el nombre del material es inválido
                    }
                }
            }
        }
        return materials;
    }

    private static List<Condition> parseConditions(Map<String, Object> properties) {
        Object rawConditions = properties.get("conditions");
        if (!(rawConditions instanceof Map)) {
            return Collections.emptyList();
        }

        List<Condition> conditions = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Map<String, Object> conditionsMap = (Map<String, Object>) rawConditions;

        // Lógica para target_mobs y not_target_mobs
        for (String key : Arrays.asList("target_mobs", "not_target_mobs")) {
            if (conditionsMap.get(key) instanceof List) {
                @SuppressWarnings("unchecked")
                Set<EntityType> mobTypes = ((List<String>) conditionsMap.get(key)).stream()
                        .map(s -> {
                            try { return EntityType.valueOf(s.toUpperCase()); }
                            catch (IllegalArgumentException e) { return null; }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!mobTypes.isEmpty()) {
                    conditions.add(new TargetMobCondition(mobTypes, key.startsWith("not_")));
                }
            }
        }

        // Lógica para spawn_reason y not_spawn_reason
        for (String key : Arrays.asList("spawn_reason", "not_spawn_reason")) {
            if (conditionsMap.get(key) instanceof List) {
                @SuppressWarnings("unchecked")
                Set<SpawnReason> reasons = ((List<String>) conditionsMap.get(key)).stream()
                        .map(s -> {
                            try { return SpawnReason.valueOf(s.toUpperCase()); }
                            catch (IllegalArgumentException e) { return null; }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!reasons.isEmpty()) {
                    conditions.add(new SpawnReasonCondition(reasons, key.startsWith("not_")));
                }
            }
        }

        // Lógica para blocks y not_blocks
        for (String key : Arrays.asList("blocks", "not_blocks")) {
            if (conditionsMap.get(key) instanceof List) {
                @SuppressWarnings("unchecked")
                Set<Material> materialTypes = ((List<String>) conditionsMap.get(key)).stream()
                        .map(s -> {
                            try { return Material.valueOf(s.toUpperCase()); }
                            catch (IllegalArgumentException e) { return null; }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!materialTypes.isEmpty()) {
                    conditions.add(new BlockCondition(materialTypes, key.startsWith("not_")));
                }
            }
        }

        // Lógica para damage_cause y not_damage_cause
        for (String key : Arrays.asList("damage_cause", "not_damage_cause")) {
            if (conditionsMap.get(key) instanceof List) {
                @SuppressWarnings("unchecked")
                Set<EntityDamageEvent.DamageCause> causes = ((List<String>) conditionsMap.get(key)).stream()
                        .map(s -> {
                            try { return EntityDamageEvent.DamageCause.valueOf(s.toUpperCase()); }
                            catch (IllegalArgumentException e) { return null; }
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());
                if (!causes.isEmpty()) {
                    conditions.add(new DamageCauseCondition(causes, key.startsWith("not_")));
                }
            }
        }

        return conditions;
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

        List<Condition> conditions = parseConditions(properties);
        int cooldown = getSafeInt(properties.get("cooldown"), 0);
        String cooldownId = (String) properties.get("cooldown_id");

        return switch (upperType) {
            case "DAMAGE" -> {
                double amount = getSafeDouble(properties.get("amount"), 5.0);
                yield new DamageEffect(amount, target, conditions, cooldown, cooldownId);
            }
            case "HEAL" -> {
                double amount = getSafeDouble(properties.get("amount"), 5.0);
                yield new HealEffect(amount, target, conditions, cooldown, cooldownId);
            }
            case "MESSAGE" -> {
                String text = (String) properties.getOrDefault("text", "Mensaje vacío");
                yield new MessageEffect(text, conditions, cooldown, cooldownId);
            }
            case "POTION" -> {
                String potionTypeName = (String) properties.getOrDefault("potion_type", "SPEED");
                PotionEffectType potionType = PotionEffectType.getByName(potionTypeName.toUpperCase());
                int duration = getSafeInt(properties.get("duration"), 100);
                int amplifier = getSafeInt(properties.get("amplifier"), 0);

                if (potionType == null) yield null;
                yield new PotionEffect(potionType, duration, amplifier, target, conditions, cooldown, cooldownId);
            }
            case "LIGHTNING" -> {
                yield new LightningEffect(target, conditions, cooldown, cooldownId);
            }
            case "BREAK_BLOCK" -> {
                int radius = getSafeInt(properties.get("radius"), 1);
                int layers = getSafeInt(properties.get("layers"), 1);
                List<Material> mineableBlocks = getSafeMaterialList(properties.get("can_mine_blocks"));
                yield new BreakBlockEffect(radius, layers, mineableBlocks, conditions, cooldown, cooldownId);
            }
            case "VEIN_MINE" -> {
                int maxBlocks = getSafeInt(properties.get("max_blocks"), 64);
                List<Material> mineableBlocks = getSafeMaterialList(properties.get("can_mine_blocks"));
                yield new VeinMineEffect(maxBlocks, mineableBlocks, conditions, cooldown, cooldownId);
            }
            case "CHAIN_LIGHTNING" -> {
                int chainCount = getSafeInt(properties.get("chain_count"), 3);
                double damage = getSafeDouble(properties.get("damage"), 5.0);
                double range = getSafeDouble(properties.get("range"), 5.0);

                String particleName = (String) properties.getOrDefault("particle_type", "ELECTRIC_SPARK");
                Particle particleType = Particle.ELECTRIC_SPARK;
                try {
                    particleType = Particle.valueOf(particleName.toUpperCase());
                } catch (IllegalArgumentException e) {}

                String soundName = (String) properties.getOrDefault("sound_effect", "ENTITY_LIGHTNING_BOLT_THUNDER");
                Sound soundEffect = Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
                try {
                    soundEffect = Sound.valueOf(soundName.toUpperCase());
                } catch (IllegalArgumentException e) {}

                yield new ChainLightningEffect(chainCount, damage, range, particleType, soundEffect, target, conditions, cooldown, cooldownId);
            }
            case "SHADOW_CLONE" -> {
                int duration = getSafeInt(properties.get("duration"), 100);
                double range = getSafeDouble(properties.get("range"), 15.0);
                String particleName = (String) properties.getOrDefault("particle_type", "CLOUD");
                Particle particleType = Particle.CLOUD;
                try {
                    particleType = Particle.valueOf(particleName.toUpperCase());
                } catch (IllegalArgumentException e) {}

                String soundName = (String) properties.getOrDefault("sound_effect", "ENTITY_VILLAGER_NO");
                Sound soundEffect = Sound.ENTITY_VILLAGER_NO;
                try {
                    soundEffect = Sound.valueOf(soundName.toUpperCase());
                } catch (IllegalArgumentException e) {}

                yield new ShadowCloneEffect(duration, range, particleType, soundEffect, conditions, cooldown, cooldownId);
            }
            case "GRAPPLING_HOOK" -> {
                double strength = getSafeDouble(properties.get("strength"), 1.5);
                yield new GrapplingHookEffect(strength, conditions, cooldown, cooldownId);
            }
            case "EXPLOSION" -> {
                float power = (float) getSafeDouble(properties.get("power"), 2.0);
                boolean setFire = (boolean) properties.getOrDefault("set_fire", false);
                boolean breakBlocks = (boolean) properties.getOrDefault("break_blocks", false);
                yield new ExplosionEffect(power, setFire, breakBlocks, target, conditions, cooldown, cooldownId);
            }
            default -> null;
        };
    }

    public static Effect createEffect(Map<?, ?> map) {
        if (map == null) return null;
        Map<String, Object> properties = new HashMap<>();
        map.forEach((k, v) -> properties.put(String.valueOf(k), v));
        String type = (String) properties.getOrDefault("type", "");
        if (type.isEmpty()) return null;
        return createEffectFromProperties(type, properties);
    }

    public static List<Effect> parseEffects(ConfigurationSection section) {
        List<Effect> effects = new ArrayList<>();
        if (section == null) return effects;
        for (String key : section.getKeys(false)) {
            if (!section.isConfigurationSection(key)) continue;
            ConfigurationSection effectSection = section.getConfigurationSection(key);

            Map<String, Object> properties = new HashMap<>(effectSection.getValues(false));
            if (effectSection.isConfigurationSection("conditions")) {
                properties.put("conditions", effectSection.getConfigurationSection("conditions").getValues(true));
            }

            String type = effectSection.getString("type", "");
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
                    @SuppressWarnings("unchecked")
                    Effect effect = createEffect((Map<String, Object>) item);
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