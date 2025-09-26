package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class PotionEffect extends TargetedEffect {

    private final PotionEffectType potionType;
    private final int duration;
    private final int amplifier;

    // The constructor now accepts the list of conditions
    public PotionEffect(PotionEffectType potionType, int duration, int amplifier, EffectTarget target, List<Condition> conditions) {
        // Pass the conditions to the parent class (TargetedEffect)
        super(target, conditions);
        this.potionType = potionType;
        this.duration = duration;
        this.amplifier = amplifier;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        // This logic is perfect, no changes needed.
        org.bukkit.potion.PotionEffect effect = new org.bukkit.potion.PotionEffect(potionType, duration, amplifier);
        target.addPotionEffect(effect);
    }

    @Override
    public String getType() {
        return "POTION";
    }
}