package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.stat.StatModifier;
import dev.aurelium.auraskills.api.stat.Stats;
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.UUID;

public class AddStatEffect extends BaseEffect {

    // Usamos los nombres exactos de AuraSkills para este enum:
    public enum OperationType {
        ADD,
        MULTIPLY,
        ADD_PERCENT
    }

    private final String statName;
    private final double amount;
    private final int durationTicks;
    private final OperationType operation;

    public AddStatEffect(String statName, double amount, int durationTicks, OperationType operation, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.statName = statName.toUpperCase();
        this.amount = amount;
        this.durationTicks = durationTicks;
        this.operation = operation;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        AuraSkillsApi auraSkills = AuraSkillsApi.get();
        if (auraSkills == null) return;

        // --- OBTENCIÓN DE LA ESTADÍSTICA (Línea que requiere NamespacedId) ---
        Stat stat;
        try {
            stat = Stats.valueOf(statName);
        } catch (IllegalArgumentException e) {
            // Usa NamespacedId para obtener la estadística custom
            stat = auraSkills.getGlobalRegistry().getStat(NamespacedId.of("auraskills", statName.toLowerCase()));
            if (stat == null) return;
        }

        // --- 2. CONVERSIÓN DIRECTA DE LA OPERACIÓN ---
        // Mapeamos nuestro ENUM al ENUM de AuraSkills para el constructor.
        AuraSkillsModifier.Operation auraOperation = switch (operation) {
            // CORRECCIÓN: Ahora usamos los nombres exactos confirmados.
            case ADD -> AuraSkillsModifier.Operation.ADD;
            case MULTIPLY -> AuraSkillsModifier.Operation.MULTIPLY;
            case ADD_PERCENT -> AuraSkillsModifier.Operation.ADD_PERCENT;
        };

        // Generamos un ID único y aplicamos
        String uniqueModifierId = context.getItemKey() + "_" + stat.getId().getKey() + "_" + UUID.randomUUID().toString().substring(0, 6);

        StatModifier modifier = new StatModifier(
                uniqueModifierId,
                stat,
                amount,
                auraOperation
        );

        modifier.makeTemporary(durationTicks, true);

        auraSkills.getUser(player.getUniqueId()).addStatModifier(modifier);
    }

    @Override
    public String getType() {
        return "ADD_STAT";
    }
}