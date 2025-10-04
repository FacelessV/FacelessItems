package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class ParticleCircleEffect extends TargetedEffect {

    private final Particle particleType;
    private final double radius;
    private final int points;
    private final double offsetY;
    private final double speed; // Mantenemos speed para darle movimiento a las partículas

    public ParticleCircleEffect(Particle particleType, double radius, int points, double offsetY, double speed, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.particleType = particleType;
        this.radius = radius;
        this.points = points;
        this.offsetY = offsetY;
        this.speed = speed;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        if (target == null) return;

        Location center = target.getLocation().clone().add(0, offsetY, 0);

        // La lógica se ejecuta UNA SOLA VEZ para dibujar el círculo instantáneamente
        for (int i = 0; i < points; i++) {
            // Cálculo trigonométrico de las coordenadas (X, Z) del círculo
            double angle = 2 * Math.PI * i / points;

            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            Location particleLoc = center.clone().add(x, 0, z);

            // Spawnea la partícula con cantidad 1 (un punto) y la velocidad configurada
            target.getWorld().spawnParticle(
                    particleType,
                    particleLoc,
                    1, // Cantidad: 1 (dibujamos un solo punto del círculo)
                    0, 0, 0, // Desviación (No queremos dispersión en un círculo perfecto)
                    speed    // Velocidad de la partícula (para que se mueva hacia afuera o rote)
            );
        }
    }

    @Override
    public String getType() {
        return "PARTICLE_CIRCLE";
    }
}