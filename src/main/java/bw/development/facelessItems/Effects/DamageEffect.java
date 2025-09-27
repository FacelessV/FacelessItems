package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.List;

public class DamageEffect extends TargetedEffect {

    private final double damage;

    public DamageEffect(double damage, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.damage = damage;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        if (user == null) {
            target.damage(damage);
            return;
        }
        target.damage(damage, user);
    }

    @Override
    public String getType() {
        return "DAMAGE";
    }
}