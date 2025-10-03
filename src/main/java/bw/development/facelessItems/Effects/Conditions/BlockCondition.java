package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Set;
import java.util.stream.Collectors;

public class BlockCondition implements Condition {

    private final Set<Material> materials;
    private final boolean isBlacklist;

    public BlockCondition(Set<Material> materials, boolean isBlacklist) {
        this.materials = materials;
        this.isBlacklist = isBlacklist;
    }

    // Código Mejorado (Más conciso, funciona en Java 16+)
    @Override
    public boolean check(EffectContext context) {
        Object blockObj = context.getData().get("broken_block");

        // Si blockObj es una instancia de Block, la variable 'block' se inicializa.
        if (blockObj instanceof Block block) {
            return this.matches(block.getType());
        }

        // Si no es un bloque o si blockObj es null, permitimos el paso de la condición.
        return true;
    }

    /**
     * Método de ayuda para comprobar un tipo de material directamente.
     */
    public boolean matches(Material material) {
        // --- CORRECCIÓN AQUÍ ---
        // Usamos 'materials' para que coincida con el nombre del campo de la clase.
        boolean matchFound = materials.contains(material);
        return isBlacklist != matchFound;
    }
}