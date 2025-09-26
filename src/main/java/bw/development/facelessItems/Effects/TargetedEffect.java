package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import java.util.List;

public abstract class TargetedEffect extends BaseEffect {

    protected final EffectTarget targetType;

    public TargetedEffect(EffectTarget targetType, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.targetType = targetType;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // El mensaje de depuración dorado ha sido eliminado de aquí.
        LivingEntity finalTarget = switch (targetType) {
            case PLAYER -> context.getUser();
            case ENTITY -> {
                if (context.getTargetEntity() instanceof LivingEntity living) {
                    yield living;
                }
                yield null;
            }
            case LIVING_ENTITY_IN_SIGHT -> {
                Entity entity = context.getUser().getTargetEntity(50, false);
                if (entity instanceof LivingEntity living) {
                    if (living.equals(context.getUser())) {
                        yield null;
                    }
                    yield living;
                }
                yield null;
            }
        };

        if (finalTarget != null) {
            applyToTarget(finalTarget, context.getUser(), context.getBukkitEvent());
        }
    }

    protected abstract void applyToTarget(LivingEntity target, Player user, Event event);

    @Override
    public abstract String getType();
}