package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.List;

public class AreaEffect extends BaseEffect {

    private final double radius;
    private final int maxTargets;
    private final List<BaseEffect> nestedEffects;
    private final EffectTarget centerTarget; // Target que define el CENTRO del AoE (usualmente PLAYER)

    public AreaEffect(double radius, int maxTargets, List<BaseEffect> nestedEffects, EffectTarget centerTarget, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.radius = radius;
        this.maxTargets = maxTargets;
        this.nestedEffects = nestedEffects;
        this.centerTarget = centerTarget;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player user = context.getUser();
        if (user == null) return;

        // 1. Determinar el punto central del Área de Efecto (AoE)
        Location center;

        if (centerTarget == EffectTarget.PLAYER) {
            center = user.getLocation();
        } else if (centerTarget == EffectTarget.LOCATION && context.getTargetEntity() != null) {
            // Si el trigger fue, por ejemplo, on_arrow_hit, el TargetEntity puede ser el punto de impacto
            center = context.getTargetEntity().getLocation();
        } else {
            // Por defecto, usamos la ubicación del usuario.
            center = user.getLocation();
        }

        // 2. Buscar entidades cercanas (limitado al radio)
        List<Entity> nearbyEntities = center.getWorld().getNearbyEntities(center, radius, radius, radius)
                .stream()
                .limit(maxTargets)
                .collect(java.util.stream.Collectors.toList());

        int targetsProcessed = 0;

        // 3. Iterar y aplicar los efectos anidados
        for (Entity entity : nearbyEntities) {
            if (targetsProcessed >= maxTargets) break;

            // Ignorar entidades no vivas (flechas, items, etc.) o al propio lanzador
            if (!(entity instanceof LivingEntity target) || entity.equals(user)) {
                continue;
            }

            // --- Creación del Nuevo Contexto ---
            // Creamos un nuevo contexto para cada entidad cercana
            EffectContext newContext = new EffectContext(
                    user,       // El lanzador sigue siendo el mismo
                    target,     // El objetivo (TargetEntity) es la entidad cercana actual
                    context.getBukkitEvent(),
                    context.getData(),
                    context.getItemKey(),
                    context.getPlugin()
            );

            // 4. Aplicar todos los efectos anidados (el corazón del Wrapper)
            for (BaseEffect nestedEffect : nestedEffects) {
                // Llamamos a apply() en el nuevo contexto.
                // El nestedEffect ahora actúa como si hubiera sido disparado individualmente
                // sobre esa entidad cercana.
                nestedEffect.apply(newContext);
            }

            targetsProcessed++;
        }
    }

    @Override
    public String getType() {
        return "AREA";
    }
}