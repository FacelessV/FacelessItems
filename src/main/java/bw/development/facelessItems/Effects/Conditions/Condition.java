package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;

/**
 * Interfaz funcional para todas las condiciones que pueden ser aplicadas a un efecto.
 */
@FunctionalInterface
public interface Condition {

    /**
     * Comprueba si la condición se cumple en el contexto dado.
     * @param context El contexto actual del efecto, conteniendo el jugador, objetivo, etc.
     * @return {@code true} si la condición se cumple y el efecto debe continuar, {@code false} en caso contrario.
     */
    boolean check(EffectContext context);
}