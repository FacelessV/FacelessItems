package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class GrapplingHookEffect extends BaseEffect {

    private final double strength;

    // --- CONSTRUCTOR UPDATED ---
    // Now accepts cooldown and cooldownId
    public GrapplingHookEffect(double strength, List<Condition> conditions, int cooldown, String cooldownId) {
        // And passes them to the parent class
        super(conditions, cooldown, cooldownId);
        this.strength = strength;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        Event event = context.getBukkitEvent();

        if (player == null || !(event instanceof PlayerFishEvent fishEvent)) {
            return;
        }

        if (fishEvent.getState() == PlayerFishEvent.State.IN_GROUND || fishEvent.getState() == PlayerFishEvent.State.CAUGHT_ENTITY) {
            if (fishEvent.getHook() == null) {
                return;
            }

            Location hookLocation = fishEvent.getHook().getLocation();

            // --- CÁLCULO DE FUERZA Y DIRECCIÓN ---
            Vector directionVector = hookLocation.toVector().subtract(player.getLocation().toVector());

            // 1. Distancia Horizontal (La clave para el cálculo de Y)
            double horizontalDistance = Math.sqrt(directionVector.getX() * directionVector.getX() + directionVector.getZ() * directionVector.getZ());

            // 2. Normalizar la dirección para obtener un vector unitario
            Vector normalizedDirection = directionVector.normalize();

            // 3. Aplicar la fuerza horizontal configurada (Factor 'strength' del YAML)
            Vector velocity = normalizedDirection.multiply(strength);

            // 4. CORRECCIÓN CRÍTICA: Impulso Vertical Dinámico (Basado en la distancia)
            // La fuerza vertical debe ser proporcional a la distancia (o la mitad de la fuerza configurada).
            // Si el gancho está lejos, necesitas un salto más alto. Usaremos un mínimo de 0.8.
            double verticalBoost = Math.max(0.8, strength * 0.4);

            // 5. Reiniciamos la velocidad y aplicamos el impulso
            player.setVelocity(new Vector(0, 0, 0)); // Reseteamos la velocidad para un tirón limpio.

            // Forzamos el componente vertical, manteniendo la fuerza horizontal intacta.
            velocity.setY(verticalBoost);

            player.setVelocity(velocity);

            // Remover el anzuelo
            fishEvent.getHook().remove();

            // Opcional: Sonido de tirón limpio
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1.0f, 2.0f);
        }
    }

    @Override
    public String getType() {
        return "GRAPPLING_HOOK";
    }
}