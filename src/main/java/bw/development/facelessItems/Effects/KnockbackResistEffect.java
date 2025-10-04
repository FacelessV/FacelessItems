package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import java.util.List;

public class KnockbackResistEffect extends BaseEffect {

    private final double resistance;

    public KnockbackResistEffect(double resistance, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        // La resistencia debe estar entre 0.0 (ninguna) y 1.0 (total)
        this.resistance = Math.max(0.0, Math.min(1.0, resistance));
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Vacío a propósito. La lógica la ejecuta el ItemEventListener.
    }

    public double getResistance() {
        return resistance;
    }

    @Override
    public String getType() {
        return "KNOCKBACK_RESIST";
    }
}