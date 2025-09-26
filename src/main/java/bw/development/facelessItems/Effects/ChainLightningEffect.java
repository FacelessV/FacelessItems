package bw.development.facelessItems.Effects;

import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.bukkit.Location;

import java.util.List;

public class ChainLightningEffect extends TargetedEffect {

    private final int chainCount;
    private final double damage;
    private final double range;

    public ChainLightningEffect(int chainCount, double damage, double range, EffectTarget target) {
        super(target);
        this.chainCount = chainCount;
        this.damage = damage;
        this.range = range;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        LivingEntity currentTarget = target;

        for (int i = 0; i < chainCount; i++) {
            // Buscar la siguiente entidad cercana
            LivingEntity nextTarget = findNextTarget(currentTarget, user);

            if (nextTarget == null) {
                break;
            }

            // Aplicar daño
            nextTarget.damage(damage, user);

            // Generar partículas entre los mobs
            spawnChainParticles(currentTarget, nextTarget);

            currentTarget = nextTarget;
        }
    }

    private LivingEntity findNextTarget(LivingEntity startTarget, Player user) {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        List<Entity> nearbyEntities = startTarget.getNearbyEntities(range, range, range);

        for (Entity entity : nearbyEntities) {
            if (entity.equals(user) || ! (entity instanceof LivingEntity)) {
                continue;
            }
            double distance = entity.getLocation().distance(startTarget.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = (LivingEntity) entity;
            }
        }
        return nearest;
    }

    private void spawnChainParticles(LivingEntity start, LivingEntity end) {
        // La corrección está en esta línea, clonamos la posición y le añadimos 1 a la altura.
        Location startLoc = start.getLocation().clone().add(0, 1, 0);
        Location endLoc = end.getLocation().clone().add(0, 1, 0);

        double distance = startLoc.distance(endLoc);
        Vector direction = endLoc.toVector().subtract(startLoc.toVector()).normalize();

        for (double d = 0; d < distance; d += 0.5) {
            Location loc = startLoc.clone().add(direction.clone().multiply(d));
            loc.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, loc, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public String getType() {
        return "CHAIN_LIGHTNING";
    }
}