package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition; // Importar
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import java.util.List; // Importar

// 1. Extiende BaseEffect en lugar de implementar Effect
public abstract class TargetedEffect extends BaseEffect {

    protected final EffectTarget targetType;

    // 2. El constructor ahora recibe y pasa las condiciones
    public TargetedEffect(EffectTarget targetType, List<Condition> conditions) {
        super(conditions); // Se las pasa al padre (BaseEffect)
        this.targetType = targetType;
    }

    // 3. Renombramos 'apply' a 'applyEffect' para que BaseEffect lo llame
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
                    // Prevenir que el efecto se aplique a uno mismo
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

    // El resto de la clase permanece exactamente igual
    protected abstract void applyToTarget(LivingEntity target, Player user, Event event);

    // getType() también permanece igual
    @Override
    public abstract String getType();
}