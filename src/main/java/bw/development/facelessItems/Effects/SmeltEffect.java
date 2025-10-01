package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import java.util.List;
import java.util.Map;
import org.bukkit.Material; // <-- Necesitarás estos imports
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;

public class SmeltEffect extends BaseEffect {
    // Los mapas de resultados y experiencia ahora viven aquí
    private static final Map<Material, Material> SMELT_RESULTS = Map.of(
            Material.RAW_IRON, Material.IRON_INGOT,
            Material.RAW_GOLD, Material.GOLD_INGOT,
            Material.RAW_COPPER, Material.COPPER_INGOT,
            Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP,
            Material.SAND, Material.GLASS,
            Material.COBBLESTONE, Material.STONE
    );
    private static final Map<Material, Integer> SMELT_EXP = Map.of(
            Material.IRON_ORE, 1, Material.DEEPSLATE_IRON_ORE, 1,
            Material.GOLD_ORE, 1, Material.DEEPSLATE_GOLD_ORE, 1,
            Material.COPPER_ORE, 1, Material.DEEPSLATE_COPPER_ORE, 1
    );

    public final boolean dropExperience;

    public SmeltEffect(boolean dropExperience, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.dropExperience = dropExperience;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Se deja vacío. La lógica principal ahora está en el ItemEventListener.
    }

    // --- NUEVOS MÉTODOS DE AYUDA ---
    public Material getSmeltedResult(Material material) {
        return SMELT_RESULTS.get(material);
    }

    public int getExperience(Material material) {
        return SMELT_EXP.getOrDefault(material, 0);
    }

    @Override
    public String getType() {
        return "SMELT";
    }
}