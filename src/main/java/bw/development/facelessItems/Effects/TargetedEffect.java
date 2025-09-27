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

// En TargetedEffect.java

    @Override
    protected void applyEffect(EffectContext context) {
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
            // --- LÍNEA AÑADIDA ---
            // Maneja cualquier otro caso (como BLOCK_IN_SIGHT) devolviendo null,
            // ya que este efecto solo funciona con entidades vivas.
            default -> null;
        };

        if (finalTarget != null) {
            applyToTarget(finalTarget, context.getUser(), context.getBukkitEvent(), context);
        }
    }

    // --- CHANGE 2: Add 'EffectContext context' to the method signature ---
    protected abstract void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context);

    @Override
    public abstract String getType();
}