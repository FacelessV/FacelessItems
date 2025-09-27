package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class LifestealEffect extends TargetedEffect {

    private final double percentage;

    public LifestealEffect(double percentage, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.percentage = percentage;
    }

    // --- CHANGE 3: The method now receives 'context' ---
    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        if (!target.equals(user)) {
            return;
        }

        // Now this line works perfectly!
        Object damageObj = context.getData().get("damage_amount");
        if (!(damageObj instanceof Number)) {
            return;
        }

        double damageDealt = ((Number) damageObj).doubleValue();
        double healAmount = damageDealt * (percentage / 100.0);

        double maxHealth = user.getMaxHealth();
        double newHealth = Math.min(user.getHealth() + healAmount, maxHealth);
        user.setHealth(newHealth);
    }

    @Override
    public String getType() {
        return "LIFESTEAL";
    }
}