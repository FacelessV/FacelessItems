package bw.development.facelessItems.Effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamageEffect extends TargetedEffect {

    private final double damage;

    public DamageEffect(double damage, EffectTarget target) {
        super(target);
        this.damage = damage;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        // Si el usuario es nulo, usamos el método de daño genérico
        if (user == null) {
            target.damage(damage);
            return;
        }

        // Si el usuario existe, aseguramos que el daño sea causado por él.
        // Esto es crucial para que otros plugins puedan detectar quién hizo el daño.
        target.damage(damage, user);
    }

    @Override
    public String getType() {
        return "DAMAGE";
    }
}