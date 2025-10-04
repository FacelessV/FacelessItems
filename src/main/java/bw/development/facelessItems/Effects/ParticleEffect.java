package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.List;

public class ParticleEffect extends TargetedEffect {

    private final Particle particleType;
    private final int amount;
    private final double offsetY;
    private final double spread;
    private final double speed;

    public ParticleEffect(Particle particleType, int amount, double offsetY, double spread, double speed, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.particleType = particleType;
        this.amount = amount;
        this.offsetY = offsetY;
        this.spread = spread;
        this.speed = speed;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        if (target == null) return;

        Location loc = target.getLocation().clone().add(0, offsetY, 0);

        // Ejecutar la explosión de partículas con dispersión y velocidad
        target.getWorld().spawnParticle(
                particleType,
                loc,
                amount,
                spread,    // offsetX
                spread,    // offsetY
                spread,    // offsetZ
                speed      // Extra (velocidad)
        );
    }

    @Override
    public String getType() {
        return "PARTICLE"; // Nombre final simplificado
    }
}