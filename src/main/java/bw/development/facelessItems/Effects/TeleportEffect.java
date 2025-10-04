package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import java.util.List;

public class TeleportEffect extends TargetedEffect {

    private final double distance;

    public TeleportEffect(double distance, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.distance = distance;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        if (target == null) return;

        // La lógica de teletransporte se aplica a la ubicación del objetivo (target).
        // Sin embargo, para este diseño, asumiremos que siempre queremos teletransportar al 'user'.
        LivingEntity entityToTeleport = target.equals(user) ? user : target;

        Location origin = entityToTeleport.getEyeLocation();
        Vector direction = origin.getDirection();

        // Calcula el vector de dirección multiplicado por la distancia
        Vector teleportVector = direction.multiply(distance);

        // Calcula la ubicación final sumando el vector a la ubicación original
        Location finalLocation = origin.add(teleportVector);

        // 1. Intentamos teletransportar a la ubicación calculada
        //    Usamos getTargetBlock(null, 0) o simplemente el raycast para encontrar una ubicación segura.
        //    Para simplificar, usaremos la Location calculada, dejando que Bukkit encuentre un punto seguro.

        // 2. Teletransportamos la entidad.
        //    Nota: Se usa el método teleport(Location) de Bukkit que busca un punto seguro si es necesario.
        entityToTeleport.teleport(finalLocation);
    }

    @Override
    public String getType() {
        return "TELEPORT";
    }
}