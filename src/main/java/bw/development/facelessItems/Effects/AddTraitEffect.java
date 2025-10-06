package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.trait.Trait; // Importación CLAVE
import dev.aurelium.auraskills.api.trait.TraitModifier; // Importación CLAVE
import dev.aurelium.auraskills.api.util.AuraSkillsModifier;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.UUID;

public class AddTraitEffect extends BaseEffect {

    public enum OperationType {
        ADD,
        MULTIPLY,
        ADD_PERCENT
    }

    private final String traitName; // Nombre del rasgo a buscar
    private final double amount;
    private final int durationTicks;
    private final OperationType operation;

    public AddTraitEffect(String traitName, double amount, int durationTicks, OperationType operation, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.traitName = traitName.toUpperCase();
        this.amount = amount;
        this.durationTicks = durationTicks;
        this.operation = operation;
    }

// AddTraitEffect.java

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        // Obtenemos la instancia del plugin para el scheduler
        dev.aurelium.auraskills.api.AuraSkillsApi auraSkills = dev.aurelium.auraskills.api.AuraSkillsApi.get();
        if (auraSkills == null) return;

        // Obtenemos el usuario de AuraSkills
        dev.aurelium.auraskills.api.user.SkillsUser user = auraSkills.getUser(player.getUniqueId());
        if (user == null) return;

        // --- 1. OBTENCIÓN DEL RASGO Y CONVERSIÓN DE OPERACIÓN (MISMO CÓDIGO) ---
        // ... (Tu código para obtener Trait y AuraOperation) ...
        dev.aurelium.auraskills.api.trait.Trait trait = auraSkills.getGlobalRegistry().getTrait(
                dev.aurelium.auraskills.api.registry.NamespacedId.of("auraskills", this.traitName.toLowerCase())
        );
        if (trait == null) return;

        dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation auraOperation = switch (this.operation) {
            case ADD -> dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation.ADD;
            case MULTIPLY -> dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation.MULTIPLY;
            case ADD_PERCENT -> dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation.ADD_PERCENT;
        };

        // Generamos un ID único (Necesario para la remoción)
        String uniqueModifierId = context.getItemKey() + "_trait_" + trait.getId().getKey() + "_" + java.util.UUID.randomUUID().toString().substring(0, 6);

        // --- 2. CREACIÓN Y APLICACIÓN DEL MODIFICADOR PERMANENTE ---
        // Creamos el modificador sin hacer la llamada a makeTemporary, dejándolo como "permanente" hasta que lo quitemos.
        dev.aurelium.auraskills.api.trait.TraitModifier modifier = new dev.aurelium.auraskills.api.trait.TraitModifier(
                uniqueModifierId,
                trait,
                this.amount,
                auraOperation
        );

        // Aplicamos el modificador
        user.addTraitModifier(modifier);

        // 🚨 3. PROGRAMACIÓN MANUAL DE LA REMOCIÓN (El Fix final)
        if (this.durationTicks > 0) {
            final String modifierKey = uniqueModifierId; // Necesitamos el ID final para la lambda
            final bw.development.facelessItems.FacelessItems pluginInstance = context.getPlugin();

            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    // Chequeamos si el jugador sigue en línea para evitar NullPointer
                    if (player.isOnline()) {
                        // Removemos el modificador de forma explícita por su clave
                        auraSkills.getUser(player.getUniqueId()).removeTraitModifier(modifierKey);
                        player.sendMessage("§e[AuraSkills] §7Tu bonus de rasgo ha terminado.");
                    }
                }
                // Programamos la remoción para después de la duración exacta en ticks
            }.runTaskLater(pluginInstance, this.durationTicks);
        }
    }

    @Override
    public String getType() {
        return "ADD_TRAIT";
    }
}