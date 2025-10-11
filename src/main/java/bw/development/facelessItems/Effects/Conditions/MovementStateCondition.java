package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.Player;

import java.util.Map;

public class MovementStateCondition implements Condition {

    private final boolean requireSneaking;
    private final boolean requireSprinting;
    private final boolean requireAirborne;

    // NOTA: El mapa de propiedades se usa para un constructor flexible desde el YAML
    public MovementStateCondition(boolean sneak, boolean sprint, boolean airborne) {
        this.requireSneaking = sneak;
        this.requireSprinting = sprint;
        this.requireAirborne = airborne;
    }

    @Override
    public boolean check(EffectContext context) {
        Player user = context.getUser();
        if (user == null) return false;

        // 1. Verificar Agachado (Sneaking)
        if (requireSneaking && !user.isSneaking()) {
            return false;
        }

        // 2. Verificar Corriendo (Sprinting)
        if (requireSprinting && !user.isSprinting()) {
            return false;
        }

        // 3. Verificar en el Aire (Airborne)
        if (requireAirborne) {
            // Se considera "airborne" si NO está tocando el suelo.
            if (user.isOnGround()) {
                return false;
            }
        }

        // Si se cumplen todas las condiciones requeridas (o no se requería ninguna), pasa.
        return true;
    }
}