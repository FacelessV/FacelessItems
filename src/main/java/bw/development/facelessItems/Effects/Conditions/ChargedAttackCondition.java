package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.Player;

public class ChargedAttackCondition implements Condition { // <-- Nombre de Clase Corregido

    private final boolean requiredStatus;

    public ChargedAttackCondition(boolean requiredStatus) {
        this.requiredStatus = requiredStatus;
    }

    @Override
    public boolean check(EffectContext context) {
        if (!(context.getUser() instanceof Player player)) {
            // Esta condición solo es válida si el usuario es un jugador.
            return false;
        }

        // --- Lógica de Detección de Ataque Cargado ---
        // El método getAttackCooldown() devuelve 1.0 si el ataque está 100% cargado.
        // Usamos un umbral de 0.95 (95%) para ser indulgentes.
        double attackProgress = player.getAttackCooldown();

        boolean isCharged = attackProgress >= 0.95;

        return isCharged == requiredStatus;
    }
}