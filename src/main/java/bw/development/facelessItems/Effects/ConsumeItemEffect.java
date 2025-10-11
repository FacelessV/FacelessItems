package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.Player;

import java.util.List;

public class ConsumeItemEffect extends BaseEffect {

    // --- CAMPO RENOMBRADO ---
    protected final String materialKey;
    protected final int amount;

    // Constructor: Ahora acepta materialKey para reflejar el uso unificado.
    public ConsumeItemEffect(String materialKey, int amount, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.materialKey = materialKey; // Usamos el nuevo nombre
        this.amount = amount;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player user = context.getUser();
        if (user == null) {
            return;
        }

        // --- Lógica de Consumo del Ítem ---
        // Se llama a takeItemFromInventory con el materialKey unificado
        boolean success = context.getPlugin().getCustomItemManager()
                .takeItemFromInventory(user.getInventory(), materialKey, amount); // <--- Uso del nombre corregido

        if (!success) {
            // Si falla, notificamos al jugador.
            // NOTA: Usar materialKey en el mensaje para mostrar el nombre del ítem/material.
            context.getPlugin().getMessageManager().sendMessage(user, "cost_item_not_found", "{item_name}", materialKey);
        }
    }

    @Override
    public String getType() {
        return "CONSUME_ITEM";
    }
}