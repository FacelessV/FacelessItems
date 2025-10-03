package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

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
    private final boolean triggerEvent;
    private SmeltEffect smeltModifier;
    private ReplantEffect replantModifier; // <-- AÑADIDO

    public VeinMineEffect(int maxBlocks, List<Material> mineableBlocks, boolean triggerEvent, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.maxBlocks = maxBlocks;
        this.mineableBlocks = mineableBlocks;
        this.triggerEvent = triggerEvent;
        this.smeltModifier = null;
        this.replantModifier = null;
    }

    // Método para que el listener o el ChainEffect le den el modificador
    public void setSmeltModifier(SmeltEffect smeltModifier) {
        this.smeltModifier = smeltModifier;
    }

    // --- AÑADIDO ---
    public void setReplantModifier(ReplantEffect replantModifier) {
        this.replantModifier = replantModifier;
    }

// VeinMineEffect.java

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

        List<Block> brokenBlocksInVein = new ArrayList<>();
        Material originalMaterial = startBlock.getType();
        Queue<Block> blocksToProcess = new LinkedList<>();
        Set<Block> processedBlocks = new HashSet<>();
        blocksToProcess.add(startBlock);
        processedBlocks.add(startBlock);

        int blocksBroken = 0;
        while (!blocksToProcess.isEmpty() && blocksBroken < maxBlocks) {
            Block currentBlock = blocksToProcess.poll();

            // --- LÓGICA DE REPLANTACIÓN ---
            boolean wasReplanted = false;
            if (replantModifier != null) {
                EffectContext blockContext = new EffectContext(player, null, context.getBukkitEvent(), Map.of("broken_block", currentBlock), context.getItemKey(), context.getPlugin());
                boolean conditionsMet = replantModifier.getConditions().stream().allMatch(c -> c.check(blockContext));

                if (conditionsMet && currentBlock.getBlockData() instanceof Ageable) {
                    // breakAndReplant ya no aplica daño a la herramienta
                    breakAndReplant(currentBlock, player, tool, context.getPlugin());
                    wasReplanted = true;
                }
            }

            if (!wasReplanted) {
                // Lógica de rotura (que ahora delega SMELT y el daño)
                breakNormally(currentBlock, player, tool, context);
            }

            brokenBlocksInVein.add(currentBlock);
            blocksBroken++;
            findNextVeinBlocks(currentBlock, originalMaterial, blocksToProcess, processedBlocks);
        }

        // --- CORRECCIÓN DE DAÑO: DAÑO ÚNICO AL FINAL ---
        if (blocksBroken > 0 && player.getGameMode() != GameMode.CREATIVE) {
            damageTool(player, tool);
        }

        context.getData().put("broken_blocks_list", brokenBlocksInVein);
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

    private void breakNormally(Block block, Player player, ItemStack tool, EffectContext context) {
        FacelessItems plugin = context.getPlugin();

        if (this.triggerEvent) {
            // La fundición ocurre en ItemEventListener.onBlockDropItem
            plugin.getItemEventListener().getAreaEffectUsers().add(player.getUniqueId());
            try {
                // Dispara BlockDropItemEvent, donde actúa la Lógica Externa de Smelt.
                player.breakBlock(block);
            } finally {
                plugin.getItemEventListener().getAreaEffectUsers().remove(player.getUniqueId());
            }
        } else {
            // Rompe sin disparar eventos, y sin fundición, ni daño.
            // El daño se aplicará una vez en applyEffect.
            if (player.getGameMode() == GameMode.CREATIVE) {
                block.breakNaturally();
            } else {
                // breakNaturally(tool) rompe el bloque sin eventos.
                // NO SE LLAMA A damageTool AQUÍ.
                block.breakNaturally(tool);
            }
        }
    }

    private void findNextVeinBlocks(Block currentBlock, Material originalMaterial, Queue<Block> blocksToProcess, Set<Block> processedBlocks) {
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

    private void damageTool(Player player, ItemStack tool) {
        // We don't damage tools in Creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if the item can be damaged
        if (tool.getItemMeta() instanceof Damageable damageable) {
            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);

            // There's a (100 / (Level + 1))% chance for the tool to take damage
            if (Math.random() * 100 < (100.0 / (unbreakingLevel + 1))) {
                // Apply 1 point of damage
                damageable.setDamage(damageable.getDamage() + 1);
                tool.setItemMeta(damageable);

                // Check if the tool broke
                if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null); // Remove the item
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                }
            }
        }
    }

    @Override
    public String getType() {
        return "VEIN_MINE";
    }

    public boolean shouldTriggerEvents() {
        return this.triggerEvent;
    }
}