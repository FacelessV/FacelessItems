package bw.development.facelessItems.Effects;

public enum EffectTarget {
    PLAYER,
    ENTITY, // Este es el objetivo de un evento de daño (on_hit)
    LIVING_ENTITY_IN_SIGHT, // Nuevo objetivo: la entidad que el jugador está mirando
    BLOCK_IN_SIGHT // <-- NUEVO OBJETIVO
}