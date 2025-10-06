package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.Material;
import org.bukkit.block.data.Ageable;

import java.util.List;

public class GrowthBoostEffect extends BaseEffect {

    private final double radius;
    private final double chance; // 0.0 a 1.0 (ej: 0.05 para 5% de chance)

    public GrowthBoostEffect(double radius, double chance, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.radius = radius;
        this.chance = chance;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // Vacío a propósito. La lógica de aceleración de crecimiento
        // debe ejecutarse en el SetEquipmentChecker de forma periódica.
    }

    // Getters necesarios para que el SetEquipmentChecker pueda leer los parámetros
    public double getRadius() {
        return radius;
    }

    public double getChance() {
        return chance;
    }

    @Override
    public String getType() {
        return "GROWTH_BOOST";
    }
}