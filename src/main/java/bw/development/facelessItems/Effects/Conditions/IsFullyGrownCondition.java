package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;

public class IsFullyGrownCondition implements Condition {

    private final boolean shouldBeFullyGrown;

    public IsFullyGrownCondition(boolean shouldBeFullyGrown) {
        this.shouldBeFullyGrown = shouldBeFullyGrown;
    }

    @Override
    public boolean check(EffectContext context) {
        Object blockObj = context.getData().get("broken_block");
        if (!(blockObj instanceof Block block)) {
            // Si no hay un bloque en el contexto, la condición no aplica.
            return false;
        }

        BlockData blockData = block.getBlockData();
        // Verificamos si el bloque es un cultivo que tiene "edad" (como trigo, patatas, etc.)
        if (blockData instanceof Ageable ageable) {
            boolean isMaxAge = ageable.getAge() == ageable.getMaximumAge();
            // La condición se cumple si el estado del cultivo coincide con lo esperado
            return isMaxAge == shouldBeFullyGrown;
        }

        // Si el bloque no es un cultivo "Ageable", la condición no se cumple.
        return false;
    }
}