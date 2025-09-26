package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.EntityType;

import java.util.Set;

public class TargetMobCondition implements Condition {

    private final Set<EntityType> entityTypes;
    private final boolean isBlacklist; // true si es 'not_target_mobs', false si es 'target_mobs'

    public TargetMobCondition(Set<EntityType> entityTypes, boolean isBlacklist) {
        this.entityTypes = entityTypes;
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean check(EffectContext context) {
        if (context.getTargetEntity() == null) {
            return true; // Si no hay objetivo, la condición no aplica
        }

        EntityType targetType = context.getTargetEntity().getType();
        boolean matchFound = entityTypes.contains(targetType);

        // Si es una lista negra (not_target_mobs), el efecto se aplica si NO hay coincidencia.
        // Si es una lista blanca (target_mobs), el efecto se aplica si SÍ hay coincidencia.
        return isBlacklist != matchFound;
    }
}