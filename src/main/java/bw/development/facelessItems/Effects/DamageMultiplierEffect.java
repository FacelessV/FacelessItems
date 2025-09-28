package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import java.util.List;
import java.util.Locale;

public class DamageMultiplierEffect extends BaseEffect {

    public enum ApplyType { INCOMING, OUTGOING, BOTH }

    public final double multiplier;
    public final ApplyType applyTo;

    public DamageMultiplierEffect(double multiplier, ApplyType applyTo, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.multiplier = multiplier;
        this.applyTo = applyTo;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Vacío. La lógica está en el ItemEventListener.
    }

    @Override
    public String getType() {
        return "DAMAGE_MULTIPLIER";
    }
}