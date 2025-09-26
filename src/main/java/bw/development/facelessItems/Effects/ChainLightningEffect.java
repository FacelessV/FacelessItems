package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

// 1. Now extends TargetedEffect (which extends BaseEffect)
public class ChainLightningEffect extends TargetedEffect {

    private final int chainCount;
    private final double damage;
    private final double range;
    private final Particle particleType;
    private final Sound soundEffect;

    // 2. The constructor now accepts the list of conditions
    public ChainLightningEffect(int chainCount, double damage, double range, Particle particleType, Sound soundEffect, EffectTarget target, List<Condition> conditions) {
        super(target, conditions); // 3. Pass conditions to the parent class
        this.chainCount = chainCount;
        this.damage = damage;
        this.range = range;
        this.particleType = particleType;
        this.soundEffect = soundEffect;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        List<LivingEntity> hitTargets = new ArrayList<>();
        hitTargets.add(target); // Add the initial target

        LivingEntity currentTarget = target;
        for (int i = 0; i < chainCount; i++) {
            LivingEntity nextTarget = findNextTarget(currentTarget, user, hitTargets);

            if (nextTarget == null) {
                break;
            }

            nextTarget.damage(damage, user);
            hitTargets.add(nextTarget); // Add the new target to the list of hit entities

            spawnChainParticles(currentTarget, nextTarget);
            nextTarget.getWorld().playSound(nextTarget.getLocation(), soundEffect, 1.0f, 1.0f);

            currentTarget = nextTarget;
        }
    }

    private LivingEntity findNextTarget(LivingEntity startTarget, Player user, List<LivingEntity> alreadyHit) {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        List<Entity> nearbyEntities = startTarget.getNearbyEntities(range, range, range);

        for (Entity entity : nearbyEntities) {
            // Improvements: Check if the entity has already been hit, is the user, or isn't a LivingEntity
            if (alreadyHit.contains(entity) || entity.equals(user) || !(entity instanceof LivingEntity)) {
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
        Location startLoc = start.getEyeLocation();
        Location endLoc = end.getEyeLocation();

        double distance = startLoc.distance(endLoc);
        Vector direction = endLoc.toVector().subtract(startLoc.toVector()).normalize();

        for (double d = 0; d < distance; d += 0.5) {
            Location loc = startLoc.clone().add(direction.clone().multiply(d));
            loc.getWorld().spawnParticle(particleType, loc, 1, 0, 0, 0, 0);
        }
    }

    @Override
    public String getType() {
        return "CHAIN_LIGHTNING";
    }
}