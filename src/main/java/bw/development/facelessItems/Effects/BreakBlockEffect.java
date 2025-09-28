package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class BreakBlockEffect extends BaseEffect {

    private static final Set<Material> TOOLS = EnumSet.of(
            Material.WOODEN_PICKAXE, Material.STONE_PICKAXE, Material.IRON_PICKAXE, Material.GOLDEN_PICKAXE, Material.DIAMOND_PICKAXE, Material.NETHERITE_PICKAXE,
            Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE, Material.DIAMOND_AXE, Material.NETHERITE_AXE,
            Material.WOODEN_SHOVEL, Material.STONE_SHOVEL, Material.IRON_SHOVEL, Material.GOLDEN_SHOVEL, Material.DIAMOND_SHOVEL, Material.NETHERITE_SHOVEL,
            Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE, Material.NETHERITE_HOE,
            Material.MACE
    );
    private static final Map<Material, Material> SMELT_RESULTS = Map.of(
            Material.RAW_IRON, Material.IRON_INGOT,
            Material.RAW_GOLD, Material.GOLD_INGOT,
            Material.RAW_COPPER, Material.COPPER_INGOT,
            Material.ANCIENT_DEBRIS, Material.NETHERITE_SCRAP,
            Material.SAND, Material.GLASS,
            Material.COBBLESTONE, Material.STONE
    );
    private static final Map<Material, Integer> SMELT_EXP = Map.of(
            Material.IRON_ORE, 1,
            Material.GOLD_ORE, 1,
            Material.COPPER_ORE, 1,
            Material.DEEPSLATE_IRON_ORE, 1,
            Material.DEEPSLATE_GOLD_ORE, 1,
            Material.DEEPSLATE_COPPER_ORE, 1
    );

    private final int radius;
    private final int layers;
    private final List<Material> mineableBlocks;
    private final int range;
    private final EffectTarget targetType;

    private SmeltEffect smeltModifier;
    private ReplantEffect replantModifier; // <-- AÑADIR NUEVO CAMPO

    public BreakBlockEffect(int radius, int layers, List<Material> mineableBlocks, int range, EffectTarget targetType, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.radius = radius;
        this.layers = layers;
        this.mineableBlocks = mineableBlocks;
        this.range = range;
        this.targetType = targetType;
        this.smeltModifier = null;
        this.replantModifier = null;
    }

    public void setSmeltModifier(SmeltEffect smeltModifier) {
        this.smeltModifier = smeltModifier;
    }

    public void setReplantModifier(ReplantEffect replantModifier) {
        this.replantModifier = replantModifier;
    }


    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        Block startBlock = null;

        if (targetType == EffectTarget.BLOCK_IN_SIGHT) {
            RayTraceResult result = player.rayTraceBlocks(range);
            if (result != null && result.getHitBlock() != null) {
                startBlock = result.getHitBlock();
            }
        } else if (context.getData().get("broken_block") instanceof Block blockFromContext) {
            startBlock = blockFromContext;
        }

        if (startBlock == null) return;
        if (!mineableBlocks.isEmpty() && !mineableBlocks.contains(startBlock.getType())) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || !TOOLS.contains(tool.getType())) return;

        List<Block> brokenBlocksInArea = new ArrayList<>();
        Vector direction = player.getEyeLocation().getDirection();

        // Find the primary axis of the player's view (X, Y, or Z)
        BlockFace primaryFace;
        double absX = Math.abs(direction.getX());
        double absY = Math.abs(direction.getY());
        double absZ = Math.abs(direction.getZ());
        if (absY > absX && absY > absZ) {
            primaryFace = direction.getY() > 0 ? BlockFace.UP : BlockFace.DOWN;
        } else if (absX > absZ) {
            primaryFace = direction.getX() > 0 ? BlockFace.EAST : BlockFace.WEST;
        } else {
            primaryFace = direction.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH;
        }

        for (int i = 0; i < layers; i++) {
            // --- LOGIC CORRECTION FOR LAYERS ---
            // Calculate the center of the current layer by moving from the start block
            Block layerCenterBlock = startBlock.getRelative(primaryFace, i);

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block currentBlock;
                        // Build the 3x3 plane based on the primary axis
                        if (primaryFace == BlockFace.UP || primaryFace == BlockFace.DOWN) {
                            currentBlock = layerCenterBlock.getRelative(x, 0, z);
                        } else if (primaryFace == BlockFace.EAST || primaryFace == BlockFace.WEST) {
                            currentBlock = layerCenterBlock.getRelative(0, y, z);
                        } else { // NORTH or SOUTH
                            currentBlock = layerCenterBlock.getRelative(x, y, 0);
                        }

                        if (currentBlock.getType().getHardness() < 0 || currentBlock.getType() == Material.AIR) {
                            continue;
                        }
                        if (!mineableBlocks.isEmpty() && !mineableBlocks.contains(currentBlock.getType())) {
                            continue;
                        }

                        boolean wasHandledByModifier = false;

                        if (replantModifier != null) {
                            EffectContext blockContext = new EffectContext(player, null, context.getBukkitEvent(), Map.of("broken_block", currentBlock), context.getItemKey(), context.getPlugin());
                            boolean conditionsMet = replantModifier.getConditions().stream().allMatch(c -> c.check(blockContext));
                            if (conditionsMet && currentBlock.getBlockData() instanceof Ageable) {
                                breakAndReplant(currentBlock, player, tool, context.getPlugin());
                                wasHandledByModifier = true;
                            }
                        }

                        if (!wasHandledByModifier) {
                            breakNormally(currentBlock, player, tool, context);
                        }

                        brokenBlocksInArea.add(currentBlock);
                    }
                }
            }
        }
        context.getData().put("broken_blocks_list", brokenBlocksInArea);
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

    private void breakNormally(Block block, Player player, ItemStack tool, EffectContext context) {
        boolean wasSmelted = false;
        if (smeltModifier != null) {
            EffectContext blockContext = new EffectContext(player, null, context.getBukkitEvent(), Map.of("broken_block", block), context.getItemKey(), context.getPlugin());
            boolean conditionsMet = smeltModifier.getConditions().stream().allMatch(c -> c.check(blockContext));
            if (conditionsMet) {
                smeltBlock(block, tool);
                wasSmelted = true;
            }
        }

        if (!wasSmelted) {
            if (player.getGameMode() == GameMode.CREATIVE) {
                block.breakNaturally();
            } else {
                block.breakNaturally(tool);
            }
        }
    }

    private void breakAndReplant(Block block, Player player, ItemStack tool, FacelessItems plugin) {
        Material cropType = block.getType();

        if (player.getGameMode() == GameMode.CREATIVE) {
            block.breakNaturally();
        } else {
            block.breakNaturally(tool);
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                if (block.getRelative(BlockFace.DOWN).getType() == Material.FARMLAND) {
                    block.setType(cropType);
                    BlockData newData = block.getBlockData();
                    if (newData instanceof Ageable newAgeable) {
                        newAgeable.setAge(0);
                        block.setBlockData(newAgeable);
                    }
                }
            }
        }.runTaskLater(plugin, 1L);
    }

    @Override
    public String getType() {
        return "BREAK_BLOCK";
    }
}