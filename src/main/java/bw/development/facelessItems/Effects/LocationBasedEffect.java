package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.RayTraceResult;

import java.util.List;

/**
 * Clase base para efectos que actúan en una ubicación específica en lugar de una entidad.
 */
public abstract class LocationBasedEffect extends BaseEffect {

    protected final EffectTarget targetType;
    protected final int range;

    public LocationBasedEffect(EffectTarget target, int range, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.targetType = target;
        this.range = range;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Location targetLocation = resolveLocation(context);
        if (targetLocation != null) {
            applyAtLocation(targetLocation, context);
        }
    }

    private Location resolveLocation(EffectContext context) {
        Player user = context.getUser();
        Event event = context.getBukkitEvent();

        // Casos generales basados en el targetType
        switch (targetType) {
            case PLAYER:
                return user != null ? user.getLocation() : null;
            case ENTITY:
                return context.getTargetEntity() != null ? context.getTargetEntity().getLocation() : null;
            case LIVING_ENTITY_IN_SIGHT:
                if (user == null) return null;
                return user.getTargetEntity(range) != null ? user.getTargetEntity(range).getLocation() : null;
            case BLOCK_IN_SIGHT:
                if (user == null) return null;
                RayTraceResult result = user.rayTraceBlocks(range);
                return (result != null && result.getHitBlock() != null) ? result.getHitBlock().getLocation().add(0.5, 0.5, 0.5) : null;
            case LOCATION:
                // Intentamos determinar la ubicación más específica del evento
                if (event instanceof ProjectileHitEvent hitEvent) {
                    return hitEvent.getHitEntity() != null ? hitEvent.getHitEntity().getLocation() : hitEvent.getEntity().getLocation();
                }
                if (context.getTargetEntity() != null) {
                    return context.getTargetEntity().getLocation();
                }
                if (context.getData().get("broken_block") instanceof Block block) {
                    return block.getLocation().add(0.5, 0.5, 0.5);
                }
                // Si no, la ubicación del jugador
                return user != null ? user.getLocation() : null;
        }

        // Valor por defecto si todo lo demás falla
        return user != null ? user.getLocation() : null;
    }

    /**
     * Aplica la lógica del efecto en la ubicación resuelta.
     * @param location La ubicación donde debe ocurrir el efecto.
     * @param context El contexto original del evento.
     */
    protected abstract void applyAtLocation(Location location, EffectContext context);
}