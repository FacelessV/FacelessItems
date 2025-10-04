package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import java.util.Set;

public class BiomeCondition implements Condition {

    private final Set<Biome> biomes;
    private final boolean isBlacklist; // true si es lista negra

    public BiomeCondition(Set<Biome> biomes, boolean isBlacklist) {
        this.biomes = biomes;
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean check(EffectContext context) {
        if (!(context.getUser() instanceof Player player)) {
            return true;
        }

        Biome currentBiome = player.getLocation().getBlock().getBiome();

        boolean matchFound = biomes.contains(currentBiome);

        // Retorna verdadero si (no es lista negra Y el bioma coincide) O (es lista negra Y el bioma NO coincide)
        return isBlacklist != matchFound;
    }
}