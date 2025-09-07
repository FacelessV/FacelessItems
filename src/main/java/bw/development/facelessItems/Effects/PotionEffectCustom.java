package bw.development.facelessItems.Effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PotionEffectCustom extends TargetedEffect {

    private final PotionEffectType type;
    private final int duration;
    private final int amplifier;

    public PotionEffectCustom(PotionEffectType type, int duration, int amplifier, EffectTarget target) {
        super(target);
        this.type = type;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Event event) {
        PotionEffect effect = new PotionEffect(type, duration, amplifier, false, true);
        target.addPotionEffect(effect, true);
    }
}
