package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.BaseEffect;

import java.util.List;
import java.util.Map;

public class ArmorSetBonus {
    private final List<BaseEffect> passiveEffects;
    private final Map<String, List<BaseEffect>> triggeredEffects;

    public ArmorSetBonus(List<BaseEffect> passiveEffects, Map<String, List<BaseEffect>> triggeredEffects) {
        this.passiveEffects = passiveEffects;
        this.triggeredEffects = triggeredEffects;
    }

    public List<BaseEffect> getPassiveEffects() {
        return passiveEffects;
    }

    public Map<String, List<BaseEffect>> getTriggeredEffects() {
        return triggeredEffects;
    }
}