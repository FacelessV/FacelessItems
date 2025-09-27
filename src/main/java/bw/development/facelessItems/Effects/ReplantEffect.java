package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import java.util.List;

/**
 * Efecto modificador para replantar cultivos. No actúa por sí solo.
 */
public class ReplantEffect extends BaseEffect {

    public ReplantEffect(List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Vacío intencionadamente. La lógica la ejecutan los efectos de minería.
    }

    @Override
    public String getType() {
        return "REPLANT";
    }
}