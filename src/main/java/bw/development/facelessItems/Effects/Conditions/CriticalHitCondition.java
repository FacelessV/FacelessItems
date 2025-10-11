package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class CriticalHitCondition implements Condition { // <-- Nombre de Clase Corregido

    private final boolean requiredStatus;

    public CriticalHitCondition(boolean requiredStatus) {
        this.requiredStatus = requiredStatus;
    }

    @Override
    public boolean check(EffectContext context) {
        if (!(context.getUser() instanceof Player) || !(context.getBukkitEvent() instanceof EntityDamageByEntityEvent)) {
            return false;
        }

        Player attacker = context.getUser();

        // --- Lógica de Detección de Golpe Crítico (Estándar de Spigot) ---
        // La comprobación más básica y común: El jugador debe estar cayendo/saltando.
        boolean isCritical = !attacker.isOnGround() && attacker.getFallDistance() > 0.0;

        // Si tienes acceso a APIs más avanzadas, puedes refinar esta lógica aquí.

        return isCritical == requiredStatus;
    }
}