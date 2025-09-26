package bw.development.facelessItems.Effects;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.List;
import java.util.Set;
import java.util.EnumSet;
import java.util.Queue;
import java.util.LinkedList;
import java.util.HashSet;

public class VeinMineEffect implements Effect {

    private static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.MACE
    );

    private final int maxBlocks;
    private final List<Material> mineableBlocks;

    public VeinMineEffect(int maxBlocks, List<Material> mineableBlocks) {
        this.maxBlocks = maxBlocks;
        this.mineableBlocks = mineableBlocks;
    }

    @Override
    public void apply(EffectContext context) {
        Player player = context.getUser();
        Block startBlock = (Block) context.getData().get("broken_block");

        if (player == null || startBlock == null) return;

        if (!mineableBlocks.isEmpty() && !mineableBlocks.contains(startBlock.getType())) {
            return;
        }

        Material originalMaterial = startBlock.getType();

        Queue<Block> blocksToProcess = new LinkedList<>();
        Set<Block> processedBlocks = new HashSet<>();

        blocksToProcess.add(startBlock);
        processedBlocks.add(startBlock);

        ItemStack tool = player.getInventory().getItemInMainHand();

        if (tool.getType().isAir() || !TOOLS.contains(tool.getType())) {
            return;
        }

        while (!blocksToProcess.isEmpty() && processedBlocks.size() < maxBlocks) {
            Block currentBlock = blocksToProcess.poll();

            currentBlock.breakNaturally(tool);

            for (BlockFace face : BlockFace.values()) {
                Block relativeBlock = currentBlock.getRelative(face);
                if (relativeBlock.getType() == originalMaterial && !processedBlocks.contains(relativeBlock)) {
                    blocksToProcess.add(relativeBlock);
                    processedBlocks.add(relativeBlock);
                }
            }
        }
    }

    @Override
    public String getType() {
        return "VEIN_MINE";
    }
}