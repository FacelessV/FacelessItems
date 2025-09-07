package bw.development.facelessItems.Effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public abstract class TargetedEffect implements Effect {

    protected final EffectTarget target;

    public TargetedEffect(EffectTarget target) {
        this.target = target;
    }

    @Override
    public void apply(Player player, Event event) {
        LivingEntity targetEntity = resolveTarget(player, event);
        if (targetEntity != null) {
            applyToTarget(targetEntity, event);
        }
    }

    protected LivingEntity resolveTarget(Player player, Event event) {
        return switch (target) {
            case PLAYER -> player;
            case ENTITY -> {
                if (event instanceof EntityDamageByEntityEvent e) {
                    yield e.getEntity() instanceof LivingEntity living ? living : null;
                } else if (event instanceof PlayerInteractEvent) {
                    yield player; // fallback al jugador
                }
                yield null;
            }
        };
    }


    // Subclases implementan esto para aplicar el efecto real
    protected abstract void applyToTarget(LivingEntity target, Event event);
}
