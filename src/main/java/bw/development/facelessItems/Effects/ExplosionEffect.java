package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.List;

public class ExplosionEffect extends TargetedEffect {

    private final float power;
    private final boolean setFire;
    private final boolean breakBlocks;

    public ExplosionEffect(float power, boolean setFire, boolean breakBlocks, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.power = power;
        this.setFire = setFire;
        this.breakBlocks = breakBlocks;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        // 'target' aquí es el ORIGEN de la explosión.
        // El último parámetro (target) es la 'fuente' de la explosión,
        // lo que previene que la entidad fuente reciba daño de su propia explosión.
        target.getWorld().createExplosion(target.getLocation(), power, setFire, breakBlocks, target);
    }

    @Override
    public String getType() {
        return "EXPLOSION";
    }
}