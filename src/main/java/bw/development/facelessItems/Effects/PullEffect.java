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

    public PullEffect(double radius, double strength, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.radius = radius;
        this.strength = strength;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        // 'target' es el punto central de la atracción (el jugador o la entidad golpeada)

        // Obtenemos las entidades cercanas al punto central
        List<Entity> nearbyEntities = target.getNearbyEntities(radius, radius, radius);

        for (Entity entity : nearbyEntities) {
            // Solo afectamos a entidades vivas que no sean el jugador que usa el ítem
            if (entity instanceof LivingEntity && !entity.equals(user)) {
                // Calculamos el vector desde la entidad hacia el punto de atracción
                Vector direction = target.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize();

                // Aplicamos una fuerza para atraer a la entidad
                entity.setVelocity(direction.multiply(strength));
            }
        }
    }

    @Override
    public String getType() {
        return "PULL";
    }
}