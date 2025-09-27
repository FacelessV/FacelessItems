package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class DashEffect extends BaseEffect {

    private final double strength;
    private final double verticalBoost;

    public DashEffect(double strength, double verticalBoost, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.strength = strength;
        this.verticalBoost = verticalBoost;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        // Obtenemos la dirección en la que mira el jugador (ignorando el eje Y para un dash horizontal)
        Vector direction = player.getLocation().getDirection().setY(0).normalize();

        // Creamos el vector de velocidad
        Vector velocity = direction.multiply(strength);

        // Añadimos el pequeño impulso vertical para evitar chocar con el suelo
        velocity.setY(verticalBoost);

        // Aplicamos la velocidad al jugador
        player.setVelocity(velocity);
    }

    @Override
    public String getType() {
        return "DASH";
    }
}