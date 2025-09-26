package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class HealEffect extends TargetedEffect {

    private final double amount;

    // --- CONSTRUCTOR UPDATED ---
    // Now accepts cooldown and cooldownId
    public HealEffect(double amount, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        // And passes them to the parent class (TargetedEffect)
        super(target, conditions, cooldown, cooldownId);
        this.amount = amount;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        // Your healing logic is perfect and needs no changes.
        double maxHealth = target.getMaxHealth();
        double newHealth = Math.min(target.getHealth() + amount, maxHealth);
        target.setHealth(newHealth);
    }

    @Override
    public String getType() {
        return "HEAL";
    }
}