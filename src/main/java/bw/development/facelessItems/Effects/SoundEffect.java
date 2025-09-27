package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.List;

public class SoundEffect extends BaseEffect {

    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final EffectTarget targetType;

    public SoundEffect(Sound sound, float volume, float pitch, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.targetType = target;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Location location = determineLocation(context);
        if (location != null && location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    private Location determineLocation(EffectContext context) {
        Event event = context.getBukkitEvent();
        // Caso especial: El sonido debe originarse en el punto de impacto de una flecha
        if (event instanceof ProjectileHitEvent hitEvent) {
            return hitEvent.getEntity().getLocation();
        }

        // Casos generales
        switch (targetType) {
            case ENTITY:
                if (context.getTargetEntity() != null) return context.getTargetEntity().getLocation();
                break;
            case PLAYER:
                if (context.getUser() != null) return context.getUser().getLocation();
                break;
            case LIVING_ENTITY_IN_SIGHT:
                Entity entityInSight = context.getUser().getTargetEntity(50);
                if (entityInSight != null) return entityInSight.getLocation();
                break;
        }

        // Si es un evento de minería, usamos la ubicación del bloque
        if (context.getData().get("broken_block") instanceof Block block) {
            return block.getLocation();
        }

        // Como último recurso, usamos la ubicación del jugador
        return context.getUser() != null ? context.getUser().getLocation() : null;
    }

    @Override
    public String getType() {
        return "SOUND";
    }
}