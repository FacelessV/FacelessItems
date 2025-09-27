package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import java.util.List;

public class MultiShotEffect extends BaseEffect {

    public final int arrowCount;
    public final double spread;
    public final boolean propagateArrowEffects; // <-- NUEVO
    public final boolean copyCustomArrowMeta;   // <-- NUEVO
    public final boolean propagateBowEffects; // <-- NUEVO

    public MultiShotEffect(int arrowCount, double spread, boolean propagateBowEffects, boolean propagateArrowEffects, boolean copyCustomArrowMeta, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.arrowCount = arrowCount;
        this.spread = spread;
        this.propagateBowEffects = propagateBowEffects; // <-- NUEVO
        this.propagateArrowEffects = propagateArrowEffects;
        this.copyCustomArrowMeta = copyCustomArrowMeta;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // La lógica sigue estando en el ItemEventListener.
    }

    @Override
    public String getType() {
        return "MULTI_SHOT";
    }
}