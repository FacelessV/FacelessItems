package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.Set;

public class DamageCauseCondition implements Condition {

    private final Set<DamageCause> damageCauses;
    private final boolean isBlacklist;

    public DamageCauseCondition(Set<DamageCause> damageCauses, boolean isBlacklist) {
        this.damageCauses = damageCauses;
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean check(EffectContext context) {
        // Obtenemos el evento del contexto
        if (!(context.getBukkitEvent() instanceof EntityDamageEvent event)) {
            // Si el evento no es de daño, esta condición no aplica
            return true;
        }

        DamageCause cause = event.getCause();
        boolean matchFound = damageCauses.contains(cause);

        return isBlacklist != matchFound;
    }
}