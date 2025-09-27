package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;

import java.util.List;

/**
 * Un efecto "modificador" que contiene la lógica para fundir drops.
 * No actúa por sí solo, sino que es utilizado por otros efectos como VeinMineEffect.
 */
public class SmeltEffect extends BaseEffect {

    public final boolean dropExperience;

    public SmeltEffect(boolean dropExperience, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.dropExperience = dropExperience;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Este método se deja vacío intencionadamente.
        // La lógica de Smelt es llamada por otros efectos (VeinMine, BreakBlock).
    }

    @Override
    public String getType() {
        return "SMELT";
    }
}