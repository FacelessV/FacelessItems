package bw.development.facelessItems.Effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

public class PotionEffect extends TargetedEffect {

    private final PotionEffectType potionType;
    private final int duration;
    private final int amplifier;

    public PotionEffect(PotionEffectType potionType, int duration, int amplifier, EffectTarget target) {
        super(target);
        this.potionType = potionType;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        // Use the full name for the Bukkit class to avoid conflicts
        org.bukkit.potion.PotionEffect effect = new org.bukkit.potion.PotionEffect(potionType, duration, amplifier);
        target.addPotionEffect(effect);
    }

    @Override
    public String getType() {
        return "POTION";
    }
}