package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.Set;
import java.util.EnumSet;

// 1. Ahora extiende BaseEffect
public class BreakBlockEffect extends BaseEffect {

    private static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.MACE
    );

    private final int radius;
    private final int layers;
    private final List<Material> mineableBlocks;

    // 2. El constructor ahora acepta la lista de condiciones
    public BreakBlockEffect(int radius, int layers, List<Material> mineableBlocks, List<Condition> conditions) {
        super(conditions); // 3. Se pasan las condiciones al constructor padre
        this.radius = radius;
        this.layers = layers;
        this.mineableBlocks = mineableBlocks;
    }

    // 4. El método 'apply' se renombra a 'applyEffect'
    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();

        if (!(context.getData().get("broken_block") instanceof Block block)) {
            return;
        }

        if (player == null) return;

        if (!mineableBlocks.isEmpty() && !mineableBlocks.contains(block.getType())) {
            return;
        }

        Location center = block.getLocation();

        Vector direction = player.getEyeLocation().getDirection();
        BlockFace face = BlockFace.SOUTH;
        if (Math.abs(direction.getX()) > Math.abs(direction.getZ())) {
            face = (direction.getX() > 0) ? BlockFace.EAST : BlockFace.WEST;
        } else {
            face = (direction.getZ() > 0) ? BlockFace.SOUTH : BlockFace.NORTH;
        }
        if (Math.abs(direction.getY()) > Math.abs(direction.getX()) && Math.abs(direction.getY()) > Math.abs(direction.getZ())) {
            face = (direction.getY() > 0) ? BlockFace.UP : BlockFace.DOWN;
        }

        for (int i = 0; i < layers; i++) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location blockToBreak = center.clone();

                        if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                            blockToBreak.add(x, y, i * face.getModZ());
                        } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                            blockToBreak.add(i * face.getModX(), y, z);
                        } else {
                            blockToBreak.add(x, i * face.getModY(), z);
                        }

                        Block currentBlock = blockToBreak.getBlock();

                        // Usamos la comprobación moderna y más segura
                        if (currentBlock.getType().getHardness() < 0 || currentBlock.getType() == Material.AIR) {
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