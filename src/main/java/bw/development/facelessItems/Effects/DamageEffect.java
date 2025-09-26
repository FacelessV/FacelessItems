package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition; // 1. Importar Condition
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.List; // 2. Importar List

public class DamageEffect extends TargetedEffect {

    private final double damage;

    // 3. El constructor ahora recibe la lista de condiciones
    public DamageEffect(double damage, EffectTarget target, List<Condition> conditions) {
        // 4. Se la pasa al constructor de la clase padre (TargetedEffect)
        super(target, conditions);
        this.damage = damage;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event) {
        // Tu lógica aquí está perfecta, no necesita ningún cambio. ✅
        if (user == null) {
            target.damage(damage);
            return;
        }
        target.damage(damage, user);
    }

    @Override
    public String getType() {
        return "DAMAGE";
    }
}