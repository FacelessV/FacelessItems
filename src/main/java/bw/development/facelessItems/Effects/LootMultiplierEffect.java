package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import java.util.List;

public class LootMultiplierEffect extends BaseEffect {

    private final double multiplier;

    public LootMultiplierEffect(double multiplier, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        // Aseguramos que el multiplicador sea al menos 1.0
        this.multiplier = Math.max(1.0, multiplier);
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Vacío a propósito. La lógica la ejecuta ItemEventListener en BlockDropItemEvent.
    }

    public double getMultiplier() {
        return multiplier;
    }

    @Override
    public String getType() {
        return "LOOT_MULTIPLIER";
    }
}