package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class GiveExperienceEffect extends TargetedEffect {

    private final int amount;

    public GiveExperienceEffect(int amount, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.amount = amount;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        if (target instanceof Player playerTarget) {
            // --- LÍNEA CORREGIDA ---
            // Usamos 'giveExp' en lugar de 'giveExperience' para compatibilidad
            playerTarget.giveExp(amount);
        }
    }

    @Override
    public String getType() {
        return "GIVE_EXPERIENCE";
    }
}