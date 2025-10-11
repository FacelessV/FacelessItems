package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

public class PullEffect extends TargetedEffect {

    private final double radius;
    private final double strength;

    // --- CONSTRUCTOR CORREGIDO: AÑADIDO 'int cooldown' ---
    public PullEffect(double radius, double strength, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        // Ahora pasamos los SEIS argumentos requeridos a la superclase (TargetedEffect)
        super(target, conditions, cooldown, cooldownId);
        this.radius = radius;
        this.strength = strength;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        // 'target' es el punto central de la atracción (el jugador en el caso de on_use).

        // 1. Obtenemos las entidades cercanas al punto central
        List<Entity> nearbyEntities = target.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {

            // Solo procesamos entidades vivas que no sean el lanzador
            if (entity instanceof LivingEntity pulledTarget && !entity.equals(user)) {

                // 2. CREACIÓN DE CONTEXTO TEMPORAL PARA EL FILTRADO
                EffectContext pullContext = new EffectContext(
                        user,
                        pulledTarget,
                        event,
                        context.getData(),
                        context.getItemKey(),
                        context.getPlugin()
                );

                // 3. CHEQUEO DE CONDICIONES (Filtro)
                boolean conditionsMet = this.conditions.stream().allMatch(condition -> condition.check(pullContext));

                if (conditionsMet) {
                    // 4. Aplicar la fuerza (SOLO si las condiciones pasaron)
                    Vector direction = target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();
                    entity.setVelocity(direction.multiply(strength));
                }
            }
        }
    }

    @Override
    public String getType() {
        return "PULL";
    }
}