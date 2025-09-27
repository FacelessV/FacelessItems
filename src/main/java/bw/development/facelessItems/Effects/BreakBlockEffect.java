package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
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

    // --- CONSTRUCTOR ACTUALIZADO ---
    public BreakBlockEffect(int radius, int layers, List<Material> mineableBlocks, int range, EffectTarget targetType, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.radius = radius;
        this.layers = layers;
        this.mineableBlocks = mineableBlocks;
        this.range = range;
        this.targetType = targetType;
        this.smeltModifier = null;
    }

    public void setSmeltModifier(SmeltEffect smeltModifier) {
        this.smeltModifier = smeltModifier;
    }

    // --- MÉTODO APPLYEFFECT ACTUALIZADO ---
    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        Block startBlock = null;

        // Lógica para determinar el bloque objetivo
        if (targetType == EffectTarget.BLOCK_IN_SIGHT) {
            // Usamos Ray Tracing para encontrar el bloque que mira el jugador
            RayTraceResult result = player.rayTraceBlocks(range);
            if (result != null && result.getHitBlock() != null) {
                startBlock = result.getHitBlock();
            }
        } else if (context.getData().get("broken_block") instanceof Block blockFromContext) {
            // Mantenemos la lógica antigua para el trigger on_mine
            startBlock = blockFromContext;
        }

        if (startBlock == null) return;

        // El resto de la lógica de romper en área
        if (!mineableBlocks.isEmpty() && !mineableBlocks.contains(startBlock.getType())) {
            return;
        }

        ItemStack tool = player.getInventory().getItemInMainHand();
        if (tool.getType().isAir() || !TOOLS.contains(tool.getType())) {
            return;
        }

        Location center = startBlock.getLocation();
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
                        Location blockToBreakLoc = center.clone();
                        if (face == BlockFace.NORTH || face == BlockFace.SOUTH) {
                            blockToBreakLoc.add(x, y, i * face.getModZ());
                        } else if (face == BlockFace.EAST || face == BlockFace.WEST) {
                            blockToBreakLoc.add(i * face.getModX(), y, z);
                        } else {
                            blockToBreakLoc.add(x, i * face.getModY(), z);
                        }

                        Block currentBlock = blockToBreakLoc.getBlock();
                        if (currentBlock.getType().getHardness() < 0 || currentBlock.getType() == Material.AIR) {
                            continue;
                        }

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
        return "BREAK_BLOCK";
    }
}