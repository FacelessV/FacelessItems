package bw.development.facelessItems.Effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;

public class HealEffect extends TargetedEffect {

    private final double amount;

    public HealEffect(double amount, EffectTarget target) {
        super(target);
        this.amount = amount;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Event event) {
        double maxHealth = target.getMaxHealth();
        double newHealth = Math.min(target.getHealth() + amount, maxHealth);
        target.setHealth(newHealth);
    }
}
