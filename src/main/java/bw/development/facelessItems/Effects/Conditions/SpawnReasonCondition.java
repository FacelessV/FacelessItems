package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Set;

public class SpawnReasonCondition implements Condition {

    private final Set<SpawnReason> spawnReasons;
    private final boolean isBlacklist;

    public SpawnReasonCondition(Set<SpawnReason> spawnReasons, boolean isBlacklist) {
        this.spawnReasons = spawnReasons;
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean check(EffectContext context) {
        if (!(context.getTargetEntity() instanceof LivingEntity target)) {
            return true; // La condición solo aplica a LivingEntity que tienen spawn reason.
        }

        SpawnReason reason = target.getEntitySpawnReason();
        boolean matchFound = spawnReasons.contains(reason);

        return isBlacklist != matchFound;
    }
}