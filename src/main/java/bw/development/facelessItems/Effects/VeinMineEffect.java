package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class VeinMineEffect extends BaseEffect {

    private static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.MACE
    );
    private static final Map<Material, Material> SMELT_RESULTS = Map.of(
            Material.IRON_ORE, Material.IRON_INGOT,
            Material.GOLD_ORE, Material.GOLD_INGOT,
            Material.COPPER_ORE, Material.COPPER_INGOT,
            Material.DEEPSLATE_IRON_ORE, Material.IRON_INGOT,
            Material.DEEPSLATE_GOLD_ORE, Material.GOLD_INGOT,
            Material.DEEPSLATE_COPPER_ORE, Material.COPPER_INGOT,
            Material.SAND, Material.GLASS,
            Material.COBBLESTONE, Material.STONE
    );
    private static final Map<Material, Integer> SMELT_EXP = Map.of(
            Material.IRON_ORE, 1,
            Material.GOLD_ORE, 1,
            Material.COPPER_ORE, 1
    );

    private final int maxBlocks;
    private final List<Material> mineableBlocks;
    private SmeltEffect smeltModifier; // Campo para guardar el modificador

    public VeinMineEffect(int maxBlocks, List<Material> mineableBlocks, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.maxBlocks = maxBlocks;
        this.mineableBlocks = mineableBlocks;
        this.smeltModifier = null; // Se inicializa como nulo
    }

    // Método para que el listener le dé el SmeltEffect
    public void setSmeltModifier(SmeltEffect smeltModifier) {
        this.smeltModifier = smeltModifier;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (!(context.getData().get("broken_block") instanceof Block startBlock)) return;
        if (player == null) return;

        if (!mineableBlocks.isEmpty() && !mineableBlocks.contains(startBlock.getType())) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || !TOOLS.contains(tool.getType())) {
            return;
        }

        Material originalMaterial = startBlock.getType();
        Queue<Block> blocksToProcess = new LinkedList<>();
        Set<Block> processedBlocks = new HashSet<>();
        blocksToProcess.add(startBlock);
        processedBlocks.add(startBlock);

        int blocksBroken = 0;
        while (!blocksToProcess.isEmpty() && blocksBroken < maxBlocks) {
            Block currentBlock = blocksToProcess.poll();
            boolean wasSmelted = false;

            if (smeltModifier != null) {
                EffectContext blockContext = new EffectContext(player, null, context.getBukkitEvent(), Map.of("broken_block", currentBlock), context.getItemKey(), context.getPlugin());
                boolean conditionsMet = true;
                for (Condition condition : smeltModifier.getConditions()) {
                    if (!condition.check(blockContext)) {
                        conditionsMet = false;
                        break;
                    }
                }
                if (conditionsMet) {
                    smeltBlock(currentBlock, tool);
                    wasSmelted = true;
                }
            }

            if (!wasSmelted) {
                if (player.getGameMode() == GameMode.CREATIVE) {
                    currentBlock.breakNaturally();
                } else {
                    currentBlock.breakNaturally(tool);
                }
            }

            blocksBroken++;
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

    private void smeltBlock(Block block, ItemStack tool) {
        World world = block.getWorld();
        Location center = block.getLocation().add(0.5, 0.5, 0.5);
        Collection<ItemStack> drops = block.getDrops(tool);

        for (ItemStack drop : drops) {
            drop.setType(SMELT_RESULTS.getOrDefault(drop.getType(), drop.getType()));
            world.dropItemNaturally(center, drop);
        }

        if (smeltModifier != null && smeltModifier.dropExperience) {
            int exp = SMELT_EXP.getOrDefault(block.getType(), 0);
            if (exp > 0) {
                world.spawn(center, ExperienceOrb.class, orb -> orb.setExperience(exp));
            }
        }
        block.setType(Material.AIR);
    }

    @Override
    public String getType() {
        return "VEIN_MINE";
    }
}