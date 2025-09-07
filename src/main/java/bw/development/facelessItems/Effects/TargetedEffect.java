package bw.development.facelessItems.Effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

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

    // Decide a quién se le aplica el efecto
    protected LivingEntity resolveTarget(Player player, Event event) {
        return switch (target) {
            case PLAYER -> player;
            case ENTITY -> {
                if (event instanceof EntityDamageByEntityEvent e) {
                    Entity damaged = e.getEntity();
                    yield (damaged instanceof LivingEntity living) ? living : null;
                } else {
                    yield null;
                }
            }
        };
    }

    // Subclases implementan esto para aplicar el efecto real
    protected abstract void applyToTarget(LivingEntity target, Event event);
}
