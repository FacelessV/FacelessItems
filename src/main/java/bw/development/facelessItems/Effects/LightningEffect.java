package bw.development.facelessItems.Effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class LightningEffect extends TargetedEffect {

    public LightningEffect(EffectTarget target) {
        super(target);
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        target.getWorld().strikeLightning(target.getLocation());
    }

    @Override
    public String getType() {
        return "LIGHTNING";
    }
}