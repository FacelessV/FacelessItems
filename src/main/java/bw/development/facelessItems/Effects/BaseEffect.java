package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;

import java.util.List;

/**
 * Una clase base abstracta para todos los efectos que maneja la lógica de condiciones.
 */
public abstract class BaseEffect implements Effect {

    protected final List<Condition> conditions;

    public BaseEffect(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public final void apply(EffectContext context) {
        // Comprobar todas las condiciones primero.
        for (Condition condition : conditions) {
            if (!condition.check(context)) {
                return; // Si una condición falla, detener la ejecución del efecto.
            }
        }
        // Si todas las condiciones pasan, ejecutar la lógica específica del efecto.
        applyEffect(context);
    }

    /**
     * Contiene la lógica específica del efecto que se ejecutará si todas las condiciones se cumplen.
     * @param context El contexto del efecto.
     */
    protected abstract void applyEffect(EffectContext context);
}