package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;

public class ChanceCondition implements Condition {

    private final double probability; // Probabilidad de 0 a 100

    public ChanceCondition(double probability) {
        this.probability = probability;
    }

    @Override
    public boolean check(EffectContext context) {
        // Math.random() devuelve un número entre 0.0 y 1.0
        // Lo multiplicamos por 100 para que esté en la misma escala que nuestra probabilidad.
        // Si el número aleatorio es menor que la probabilidad definida, la condición se cumple.
        return Math.random() * 100.0 < probability;
    }
}