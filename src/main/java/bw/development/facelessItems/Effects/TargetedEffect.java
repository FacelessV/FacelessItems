package bw.development.facelessItems.Effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity; // Importación necesaria
import org.bukkit.event.Event;

public abstract class TargetedEffect implements Effect {

    protected final EffectTarget targetType;

    public TargetedEffect(EffectTarget targetType) {
        this.targetType = targetType;
    }

    @Override
    public void apply(EffectContext context) {
        LivingEntity finalTarget = switch (targetType) {
            case PLAYER -> context.getUser();
            case ENTITY -> {
                if (context.getTargetEntity() instanceof LivingEntity living) {
                    yield living;
                }
                yield null;
            }
            case LIVING_ENTITY_IN_SIGHT -> {
                // Obtenemos la entidad que el jugador está mirando
                Entity entity = context.getUser().getTargetEntity(50, false);

                // Verificamos si la entidad es una LivingEntity antes de usarla
                if (entity instanceof LivingEntity living) {
                    yield living;
                }
                yield null; // Devolvemos null si el objetivo no es válido
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