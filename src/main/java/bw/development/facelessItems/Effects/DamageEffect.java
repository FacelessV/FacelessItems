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
    protected void applyToTarget(LivingEntity target, Event event) {
        target.damage(damage);
    }
}

