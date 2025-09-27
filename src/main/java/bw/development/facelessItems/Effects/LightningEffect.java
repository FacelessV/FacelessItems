package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class LightningEffect extends TargetedEffect {

    // --- CONSTRUCTOR UPDATED ---
    // Now accepts cooldown and cooldownId
    public LightningEffect(EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        // And passes them to the parent class
        super(target, conditions, cooldown, cooldownId);
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        // This logic is perfect and needs no changes.
        target.getWorld().strikeLightning(target.getLocation());
    }

    @Override
    public String getType() {
        return "LIGHTNING";
    }
}