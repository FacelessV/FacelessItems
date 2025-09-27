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
import java.util.Map;

public class ChainLightningEffect extends TargetedEffect {

    private final int chainCount;
    private final double damage;
    private final double range;
    private final Particle particleType;
    private final Sound soundEffect;

    public ChainLightningEffect(int chainCount, double damage, double range, Particle particleType, Sound soundEffect, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.chainCount = chainCount;
        this.damage = damage;
        this.range = range;
        this.particleType = particleType;
        this.soundEffect = soundEffect;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        List<LivingEntity> hitTargets = new ArrayList<>();
        hitTargets.add(target);

        LivingEntity currentTarget = target;
        // The first hit on the initial target is already confirmed by the BaseEffect conditions.
        // We only need to start the chain from the second link.
        for (int i = 0; i < chainCount -1; i++) { // Note: chainCount - 1 because the first hit is the trigger
            // --- ¡LÓGICA MEJORADA! ---
            // Le pasamos el contexto original para que pueda comprobar las condiciones
            LivingEntity nextTarget = findNextTarget(currentTarget, user, hitTargets, context);

            if (nextTarget == null) {
                break;
            }

            nextTarget.damage(damage, user);
            hitTargets.add(nextTarget);

            spawnChainParticles(currentTarget, nextTarget);
            nextTarget.getWorld().playSound(nextTarget.getLocation(), soundEffect, 1.0f, 1.0f);

            currentTarget = nextTarget;
        }
    }

    private LivingEntity findNextTarget(LivingEntity startTarget, Player user, List<LivingEntity> alreadyHit, EffectContext originalContext) {
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;

        List<Entity> nearbyEntities = startTarget.getNearbyEntities(range, range, range);

        for (Entity entity : nearbyEntities) {
            if (alreadyHit.contains(entity) || entity.equals(user) || !(entity instanceof LivingEntity livingEntity)) {
                continue;
            }

            // --- ¡COMPROBACIÓN DE CONDICIONES! ---
            // Creamos un contexto temporal para el 'posible' siguiente objetivo
            EffectContext nextTargetContext = new EffectContext(user, livingEntity, originalContext.getBukkitEvent(), Map.of(), originalContext.getItemKey(), originalContext.getPlugin());

            // Verificamos si este nuevo objetivo cumple las condiciones originales del efecto
            boolean conditionsMet = true;
            for (Condition condition : this.getConditions()) { // Usamos las condiciones de ESTE efecto
                if (!condition.check(nextTargetContext)) {
                    conditionsMet = false;
                    break;
                }
            }

            if (!conditionsMet) {
                continue; // Si no cumple las condiciones, lo ignoramos
            }

            // Si cumple las condiciones, lo consideramos un objetivo válido
            double distance = entity.getLocation().distance(startTarget.getLocation());
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearest = livingEntity;
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