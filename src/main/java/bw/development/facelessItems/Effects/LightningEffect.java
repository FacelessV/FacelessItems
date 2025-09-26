package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class LightningEffect extends TargetedEffect {

    // The constructor now accepts the list of conditions
    public LightningEffect(EffectTarget target, List<Condition> conditions) {
        // Pass the conditions to the parent class
        super(target, conditions);
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        // This logic is perfect, no changes needed.
        target.getWorld().strikeLightning(target.getLocation());
    }

    @Override
    public String getType() {
        return "LIGHTNING";
    }
}