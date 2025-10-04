package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import java.util.List;

public class DamageMultiplierEffect extends BaseEffect {

    private final double multiplier;

    // ELIMINAMOS ApplyType del constructor
    public DamageMultiplierEffect(double multiplier, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.multiplier = multiplier;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Modificador pasivo
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String getType() {
        return "DAMAGE_MULTIPLIER";
    }
}