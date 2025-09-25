package bw.development.facelessItems.Effects;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.Set;
import java.util.EnumSet;

public class BreakBlockEffect implements Effect {

    private static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.MACE
    );

    private final int radius;
    private final int layers;

    public BreakBlockEffect(int radius, int layers) {
        this.radius = radius;
        this.layers = layers;
    }

    @Override
    public void apply(EffectContext context) {
        Player player = context.getUser();
        Block block = (Block) context.getData().get("broken_block");

        if (player == null || block == null) return;

        // Determinar la dirección de la minería
        BlockFace face = block.getFace(block); // Obtiene la cara del bloque

        // Iterar en un plano 2D perpendicular a la cara del bloque
        Location center = block.getLocation();

        for (int i = 0; i < layers; i++) {
            Location currentLayer = center.clone().add(face.getModX() * i, face.getModY() * i, face.getModZ() * i);

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block currentBlock = currentLayer.clone().add(x, y, z).getBlock();

                        // Si la cara es horizontal, no moverse en Y
                        if (face.getModX() != 0 || face.getModZ() != 0) {
                            if (currentBlock.getY() != currentLayer.getY()) {
                                continue;
                            }
                        }

                        // Si la cara es vertical, no moverse en X y Z
                        if (face.getModY() != 0) {
                            if (currentBlock.getX() != currentLayer.getX() || currentBlock.getZ() != currentLayer.getZ()) {
                                continue;
                            }
                        }

                        if (currentBlock.getType().getHardness() <= -1) {
                            continue;
                        }

                        ItemStack tool = player.getInventory().getItemInMainHand();
                        if (tool.getType().isAir() || !TOOLS.contains(tool.getType())) {
                            continue;
                        }

                        if (player.getGameMode() == GameMode.CREATIVE) {
                            currentBlock.breakNaturally();
                        } else {
                            currentBlock.breakNaturally(tool);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getType() {
        return "BREAK_BLOCK";
    }
}