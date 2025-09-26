package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;

public class BlockCondition implements Condition {

    private final Set<Material> materials;
    private final boolean isBlacklist;

    public BlockCondition(Set<Material> materials, boolean isBlacklist) {
        this.materials = materials;
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean check(EffectContext context) {
        // Obtenemos el bloque del contexto, que se añade en el evento on_mine
        Object blockObj = context.getData().get("broken_block");

        // Si no hay un bloque en el contexto, esta condición no aplica.
        if (!(blockObj instanceof Block block)) {
            return true;
        }

        Material blockType = block.getType();
        boolean matchFound = materials.contains(blockType);

        // Si es lista negra, retorna true si NO hay coincidencia.
        // Si es lista blanca, retorna true si SÍ hay coincidencia.
        return isBlacklist != matchFound;
    }
}