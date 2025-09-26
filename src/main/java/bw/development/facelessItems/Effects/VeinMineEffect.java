package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.GameMode; // Import GameMode

import java.util.*;

// 1. Now extends BaseEffect
public class VeinMineEffect extends BaseEffect {

    private static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.MACE
    );

    private final int maxBlocks;
    private final List<Material> mineableBlocks;

    // 2. The constructor now accepts the list of conditions
    public VeinMineEffect(int maxBlocks, List<Material> mineableBlocks, List<Condition> conditions) {
        super(conditions); // 3. Pass conditions to the parent class
        this.maxBlocks = maxBlocks;
        this.mineableBlocks = mineableBlocks;
    }

    // 4. Renamed 'apply' to 'applyEffect'
    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();

        if (!(context.getData().get("broken_block") instanceof Block startBlock)) {
            return;
        }

        if (player == null) return;

        // --- Your excellent BFS logic remains unchanged ---
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

        int blocksBroken = 0; // Use a counter to respect maxBlocks
        while (!blocksToProcess.isEmpty() && blocksBroken < maxBlocks) {
            Block currentBlock = blocksToProcess.poll();

            // Handle creative mode correctly
            if (player.getGameMode() == GameMode.CREATIVE) {
                currentBlock.breakNaturally();
            } else {
                currentBlock.breakNaturally(tool);
            }
            blocksBroken++;

            // Search in all 26 directions for a more thorough vein mine
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;

                        Block relativeBlock = currentBlock.getRelative(x, y, z);
                        if (relativeBlock.getType() == originalMaterial && processedBlocks.add(relativeBlock)) {
                            blocksToProcess.add(relativeBlock);
                        }
                    }
                }
            }
        }
    }

    @Override
    public String getType() {
        return "VEIN_MINE";
    }
}