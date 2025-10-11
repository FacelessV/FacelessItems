package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.Player;

public class HealthThresholdCondition implements Condition {

    private final double threshold; // Umbral de salud (0.0 a 1.0)

    // NOTA: Mantenemos el campo checkBelow solo si quieres usar health_above también,
    // si solo vas a usar health_below, puedes simplificar la clase.
    private final boolean checkBelow;

    /**
     * Constructor para la Condición de Umbral de Salud.
     * @param threshold Porcentaje de salud (0.0 a 1.0, ej. 0.30).
     * @param checkBelow Define si la condición se cumple cuando la vida es MENOR (true) o MAYOR (false) al umbral.
     */
    public HealthThresholdCondition(double threshold, boolean checkBelow) {
        this.threshold = threshold;
        this.checkBelow = checkBelow;
    }

    @Override
    public boolean check(EffectContext context) {
        Player user = context.getUser();
        if (user == null) {
            return false;
        }

        double maxHealth = user.getMaxHealth();
        double currentHealth = user.getHealth();
        double currentPercentage = currentHealth / maxHealth;

        if (checkBelow) {
            // Condición: Se activa si la salud actual es MENOR al umbral (0.30)
            return currentPercentage < threshold;
        } else {
            // Condición: Se activa si la salud actual es MAYOR o igual al umbral (0.70)
            return currentPercentage >= threshold;
        }
    }
}