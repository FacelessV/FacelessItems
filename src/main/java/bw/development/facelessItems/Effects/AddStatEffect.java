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

    // Se mantiene el enum de operación
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

        dev.aurelium.auraskills.api.user.SkillsUser user = auraSkills.getUser(player.getUniqueId());
        if (user == null) return;

        // --- 1. OBTENCIÓN DE LA ESTADÍSTICA ---
        Stat stat;
        try {
            stat = Stats.valueOf(statName); // Intenta obtener Vanilla Stat (obsoleto, pero sigue siendo útil)
        } catch (IllegalArgumentException e) {
            // Usa NamespacedId para obtener la estadística custom
            stat = auraSkills.getGlobalRegistry().getStat(NamespacedId.of("auraskills", statName.toLowerCase()));
            if (stat == null) return;
        }

        // Mapeamos nuestro ENUM al ENUM de AuraSkills
        AuraSkillsModifier.Operation auraOperation = switch (operation) {
            case ADD -> AuraSkillsModifier.Operation.ADD;
            case MULTIPLY -> AuraSkillsModifier.Operation.MULTIPLY;
            case ADD_PERCENT -> AuraSkillsModifier.Operation.ADD_PERCENT;
        };

        // Generamos un ID único (CLAVE para la remoción)
        // Usamos "_stat_" en lugar de "_trait_" para diferenciar en el ID.
        String uniqueModifierId = context.getItemKey() + "_stat_" + stat.getId().getKey() + "_" + UUID.randomUUID().toString().substring(0, 6);

        // --- 2. CREACIÓN Y APLICACIÓN DEL MODIFICADOR ---
        // Lo creamos como PERMANENTE inicialmente
        StatModifier modifier = new StatModifier(
                uniqueModifierId,
                stat,
                amount,
                auraOperation
        );

        // Aplicamos el modificador
        user.addStatModifier(modifier);


        // 🚨 3. PROGRAMACIÓN MANUAL DE LA REMOCIÓN (EL FIX)
        if (this.durationTicks > 0) {
            final String modifierKey = uniqueModifierId; // Necesitamos el ID final para la lambda
            final bw.development.facelessItems.FacelessItems pluginInstance = context.getPlugin();

            new BukkitRunnable() {
                @Override
                public void run() {
                    // Solo removemos si el jugador sigue online
                    if (player.isOnline()) {
                        // Removemos el modificador de forma explícita por su clave
                        auraSkills.getUser(player.getUniqueId()).removeStatModifier(modifierKey);
                    }
                }
                // Programamos la remoción para después de la duración exacta en ticks
            }.runTaskLater(pluginInstance, this.durationTicks);
        }
    }

    @Override
    public String getType() {
        return "ADD_STAT";
    }
}