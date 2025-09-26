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
    private final Particle particleType; // <-- Nuevo campo

    public ChainLightningEffect(int chainCount, double damage, double range, Particle particleType, EffectTarget target) {
        super(target);
        this.chainCount = chainCount;
        this.damage = damage;
        this.range = range;
        this.particleType = particleType;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        LivingEntity currentTarget = target;

        for (int i = 0; i < chainCount; i++) {
            LivingEntity nextTarget = findNextTarget(currentTarget, user);

            if (nextTarget == null) {
                break;
            }

            nextTarget.damage(damage, user);

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
        Location startLoc = start.getLocation().clone().add(0, 1, 0);
        Location endLoc = end.getLocation().clone().add(0, 1, 0);

        double distance = startLoc.distance(endLoc);
        Vector direction = endLoc.toVector().subtract(startLoc.toVector()).normalize();

        for (double d = 0; d < distance; d += 0.5) {
            Location loc = startLoc.clone().add(direction.clone().multiply(d));
            loc.getWorld().spawnParticle(particleType, loc, 1, 0, 0, 0, 0); // <-- Usamos el nuevo campo aquí
        }
    }

    @Override
    public String getType() {
        return "CHAIN_LIGHTNING";
    }
}