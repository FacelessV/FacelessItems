package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

// --- 1. EXTENDER DE TARGETEDEFFECT ---
public class KnockbackEffect extends TargetedEffect {

    private final double strength;

    // Constructor: Ahora llamamos al constructor de TargetedEffect
    public KnockbackEffect(double strength, EffectTarget targetType, List<Condition> conditions, int cooldown, String cooldownId) {
        super(targetType, conditions, cooldown, cooldownId);
        this.strength = strength;
    }

    // --- 2. IMPLEMENTAR EL MÉTODO applyToTarget ---
    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {

        // La lógica de applyToTarget se ejecuta SOLAMENTE si:
        // 1. Condiciones y cooldown pasaron (vía BaseEffect).
        // 2. Se encontró un LivingEntity válido (vía TargetedEffect).

        // Si el usuario no es un jugador (aunque debería serlo por el contexto), salimos.
        if (user == null) {
            return;
        }

        // --- Lógica de Manipulación de Vector (Empuje) ---
        try {
            Vector direction;

            // CASO A: Empuje al jugador (KNOCKBACK sobre sí mismo, como un DASH).
            if (target.equals(user)) {
                // Empuje hacia adelante/arriba
                direction = user.getLocation().getDirection().multiply(strength).setY(0.5);
            }

            // CASO B: Empuje a una entidad externa.
            else {
                // 3. Calcular la dirección de empuje: Lejos del jugador.
                // Vector = Posición del Objetivo - Posición del Jugador
                direction = target.getLocation().toVector()
                        .subtract(user.getLocation().toVector())
                        .normalize();

                // 4. Aplicar la fuerza y el impulso vertical
                direction.multiply(strength);

                // 5. Ajuste vertical: Vital para que el empuje funcione bien en Minecraft.
                // Aseguramos que el componente vertical (Y) sea al menos 0.5.
                direction.setY(Math.max(direction.getY() / 2.0, 0.5));
            }

            // 6. Aplicar la velocidad
            target.setVelocity(direction);

        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error aplicando KNOCKBACK a " + target.getName() + " en " + context.getItemKey() + ": " + e.getMessage());
        }
    }

    @Override
    public String getType() {
        return "KNOCKBACK";
    }
}