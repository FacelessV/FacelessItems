package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import java.util.List;

public class TauntEffect extends BaseEffect {

    private final double radius;
    private final boolean ignoreLOS;

    public TauntEffect(double radius, boolean ignoreLOS, EffectTarget targetType, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.radius = radius;
        this.ignoreLOS = ignoreLOS;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        // Obtenemos las condiciones definidas en el YAML (incluyendo TargetMobCondition)
        List<Condition> effectConditions = getConditions();

        // 1. Iteramos sobre las entidades cercanas
        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {

            // Solo afectamos a entidades que tienen una IA y pueden tener un objetivo.
            if (entity instanceof Mob mob) {

                // --- 2. CREACIÓN DE CONTEXTO DE VERIFICACIÓN ---
                // Creamos un contexto temporal para verificar las condiciones contra este mob específico.
                EffectContext mobContext = new EffectContext(
                        player,
                        entity, // El mob actual es el target para la verificación de condiciones
                        context.getBukkitEvent(),
                        context.getData(),
                        context.getItemKey(),
                        context.getPlugin()
                );

                // --- 3. VERIFICACIÓN DE CONDICIONES (Ej: target_mobs: ZOMBIE) ---
                boolean conditionsMet = effectConditions.stream().allMatch(c -> c.check(mobContext));

                if (!conditionsMet) {
                    continue; // Saltar si el mob no cumple con las condiciones (Ej: no es un Zombi)
                }

                // --- 4. VERIFICACIÓN DE LINEA DE VISIÓN (LoS) ---
                if (!ignoreLOS) {
                    if (!mob.hasLineOfSight(player)) {
                        continue; // Saltar si el mob no puede ver al jugador
                    }
                }

                // 5. Aplicar la provocación
                mob.setTarget(player);
            }
        }
    }

    @Override
    public String getType() {
        return "TAUNT";
    }
}