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

    public static double getSafeDouble(Object raw, double defaultValue) {
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

    public static int getSafeInt(Object raw, int defaultValue) {
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

        if (conditionsMap.containsKey("chance")) {
            // Usamos getSafeDouble para leer el número de forma segura
            double probability = getSafeDouble(conditionsMap.get("chance"), 100.0); // 100% por defecto si el valor es inválido
            if (probability > 0 && probability <= 100) {
                conditions.add(new ChanceCondition(probability));
            }
        }

        for (String key : Arrays.asList("worlds", "not_worlds")) {
            if (conditionsMap.get(key) instanceof List) { // Comprobamos que es una lista
                @SuppressWarnings("unchecked")
                // Convertimos el Object a una List<String>
                List<String> worldList = (List<String>) conditionsMap.get(key);
                // Creamos el Set a partir de la lista ya convertida
                Set<String> worldNames = new HashSet<>(worldList);

                if (!worldNames.isEmpty()) {
                    conditions.add(new WorldCondition(worldNames, key.startsWith("not_")));
                }
            }
        }

        if (conditionsMap.containsKey("is_day")) {
            // We get the boolean value (true for day, false for night)
            boolean mustBeDay = (boolean) conditionsMap.getOrDefault("is_day", true);
            conditions.add(new TimeCondition(mustBeDay));
        }

        if (conditionsMap.containsKey("is_fully_grown")) {
            boolean value = (boolean) conditionsMap.getOrDefault("is_fully_grown", true);
            conditions.add(new IsFullyGrownCondition(value));
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
                List<Material> mineableBlocks = getSafeMaterialList(properties.get("mineable_blocks"));
                int range = getSafeInt(properties.get("range"), 10);

                yield new BreakBlockEffect(radius, layers, mineableBlocks, range, target, conditions, cooldown, cooldownId);
            }
            case "VEIN_MINE" -> {
                int maxBlocks = getSafeInt(properties.get("max_blocks"), 64);
                List<Material> mineableBlocks = getSafeMaterialList(properties.get("mineable_blocks"));

                // --- ¡CORRECCIÓN! ---
                // Llamamos al constructor simple, que ya no necesita 'smelt' ni 'experience'.
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
            case "CHAIN" -> {
                List<BaseEffect> effectsInChain = new ArrayList<>();
                int delay = getSafeInt(properties.get("delay"), 0);

                Object rawChainedEffects = properties.get("effects");

                // Obtenemos el target del 'CHAIN' padre para pasárselo a los hijos.
                String parentTargetStr = (String) properties.getOrDefault("target", "PLAYER");

                if (rawChainedEffects instanceof List) {
                    List<Map<String, Object>> effectsMaps = (List<Map<String, Object>>) rawChainedEffects;

                    for(Map<String, Object> effectMap : effectsMaps) {
                        // --- ¡LÓGICA CLAVE! ---
                        // Si el efecto hijo no tiene su propio 'target', hereda el del padre.
                        effectMap.putIfAbsent("target", parentTargetStr);

                        // Creamos el efecto con la información de target actualizada
                        Effect effect = createEffect((Map<?, ?>) effectMap);
                        if(effect instanceof BaseEffect){
                            effectsInChain.add((BaseEffect) effect);
                        }
                    }

                } else if (rawChainedEffects instanceof ConfigurationSection) {
                    // Esta lógica es más compleja, nos centramos en la de arriba que es la que usas
                    // pero necesitaría una adaptación similar.
                }

                yield new ChainEffect(effectsInChain, delay, conditions, cooldown, cooldownId);
            }
            case "LIFESTEAL" -> {
                double percentage = getSafeDouble(properties.get("percentage"), 10.0); // 10% por defecto
                // El robo de vida siempre se aplica al jugador
                yield new LifestealEffect(percentage, EffectTarget.PLAYER, conditions, cooldown, cooldownId);
            }
            case "CURE_ZOMBIFICATION" -> {
                int duration = getSafeInt(properties.get("cure_duration"), 200); // 10 segundos por defecto
                yield new CureZombificationEffect(duration, target, conditions, cooldown, cooldownId);
            }
            case "DASH" -> {
                double strength = getSafeDouble(properties.get("strength"), 2.0);
                double verticalBoost = getSafeDouble(properties.get("vertical_boost"), 0.2);
                yield new DashEffect(strength, verticalBoost, conditions, cooldown, cooldownId);
            }

            case "PULL" -> {
                double radius = getSafeDouble(properties.get("radius"), 8.0);
                double strength = getSafeDouble(properties.get("strength"), 1.5);
                yield new PullEffect(radius, strength, target, conditions, cooldown, cooldownId);
            }

            case "MULTI_SHOT" -> {
                int arrowCount = getSafeInt(properties.get("arrow_count"), 3);
                double spread = getSafeDouble(properties.get("spread"), 10.0);
                // Leemos los nuevos booleanos, con 'true' como valor por defecto
                boolean propagate = (boolean) properties.getOrDefault("propagate_arrow_effects", true);
                boolean copyMeta = (boolean) properties.getOrDefault("copy_custom_arrow_meta", true);

                yield new MultiShotEffect(arrowCount, spread, propagate, copyMeta, conditions, cooldown, cooldownId);
            }

            case "SMELT" -> {
                boolean dropExperience = (boolean) properties.getOrDefault("drop_experience", true);
                // Smelt ahora usa sus propias condiciones, que se parsean automáticamente
                yield new SmeltEffect(dropExperience, conditions, cooldown, cooldownId);
            }

            case "SOUND" -> {
                String soundName = (String) properties.getOrDefault("sound_effect", "UI_BUTTON_CLICK");
                Sound sound;
                try {
                    sound = Sound.valueOf(soundName.toUpperCase());
                } catch (IllegalArgumentException e) { sound = Sound.UI_BUTTON_CLICK; }

                float volume = (float) getSafeDouble(properties.get("volume"), 1.0);
                float pitch = (float) getSafeDouble(properties.get("pitch"), 1.0);
                int range = getSafeInt(properties.get("range"), 20); // <-- AÑADIR ESTA LÍNEA

                yield new SoundEffect(sound, volume, pitch, target, range, conditions, cooldown, cooldownId);
            }

            case "REPLANT" -> {
                yield new ReplantEffect(conditions, cooldown, cooldownId);
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