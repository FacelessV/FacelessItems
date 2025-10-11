package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.Player;

public class InventoryStatusCondition implements Condition {

    private final String materialKey; // <-- RENOMBRADO para ser consistente con el nuevo estándar
    private final int requiredAmount;

    public InventoryStatusCondition(String materialKey, int requiredAmount) {
        this.materialKey = materialKey;
        this.requiredAmount = requiredAmount;
    }

    @Override
    public boolean check(EffectContext context) {
        if (!(context.getUser() instanceof Player user)) {
            // Esta condición solo se aplica a jugadores
            return false;
        }

        // --- Lógica de Conteo ---
        // Llamamos al método unificado, pasándole la clave genérica (materialKey).
        int currentCount = context.getPlugin().getCustomItemManager()
                .countItemInInventory(user, materialKey); // <-- Uso de materialKey

        // La condición se cumple si la cantidad actual es MAYOR o IGUAL a la requerida.
        return currentCount >= requiredAmount;
    }
}