package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.RayTraceResult;

import java.util.List;

public class SoundEffect extends BaseEffect {

    private final Sound sound;
    private final float volume;
    private final float pitch;
    private final EffectTarget targetType;
    private final int range; // <-- Se añade rango para el BLOCK_IN_SIGHT

    public SoundEffect(Sound sound, float volume, float pitch, EffectTarget target, int range, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
        this.targetType = target;
        this.range = range; // <-- Se guarda el rango
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Location location = determineLocation(context);
        if (location != null && location.getWorld() != null) {
            location.getWorld().playSound(location, sound, volume, pitch);
        }
    }

    private Location determineLocation(EffectContext context) {
        Player user = context.getUser();
        Event event = context.getBukkitEvent();

        if (event instanceof ProjectileHitEvent hitEvent) {
            return hitEvent.getEntity().getLocation();
        }

        // --- LÓGICA CORREGIDA ---
        switch (targetType) {
            case PLAYER:
                return user != null ? user.getLocation() : null;
            case ENTITY:
                return context.getTargetEntity() != null ? context.getTargetEntity().getLocation() : null;
            case LIVING_ENTITY_IN_SIGHT:
                Entity entityInSight = user != null ? user.getTargetEntity(50) : null;
                return entityInSight != null ? entityInSight.getLocation() : null;
            case BLOCK_IN_SIGHT:
                if (user == null) return null;
                // Ahora SoundEffect también puede hacer Ray Tracing
                RayTraceResult result = user.rayTraceBlocks(range);
                return (result != null && result.getHitBlock() != null) ? result.getHitBlock().getLocation() : null;
        }

        if (context.getData().get("broken_block") instanceof Block block) {
            return block.getLocation();
        }

        return user != null ? user.getLocation() : null;
    }

    @Override
    public String getType() {
        return "SOUND";
    }
}