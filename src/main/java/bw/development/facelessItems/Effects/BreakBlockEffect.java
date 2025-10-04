package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BreakBlockEffect extends BaseEffect {

    private final int radius;
    private final int layers;
    private final List<Material> mineableBlocks;
    private final int range;
    private final EffectTarget targetType;
    private final boolean triggerEvent;
    private ReplantEffect replantModifier;

    public BreakBlockEffect(int radius, int layers, List<Material> mineableBlocks, int range, EffectTarget targetType, boolean triggerEvent, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.radius = radius;
        this.layers = layers;
        this.mineableBlocks = mineableBlocks;
        this.range = range;
        this.targetType = targetType;
        this.triggerEvent = triggerEvent;
        this.replantModifier = null;
    }

    public void setReplantModifier(ReplantEffect replantModifier) {
        this.replantModifier = replantModifier;
    }

    public boolean shouldTriggerEvents() {
        return this.triggerEvent;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        List<Block> blocksToProcess = findBlocksToBreak(context);
        if (blocksToProcess.isEmpty()) return;

        ItemStack tool = player.getInventory().getItemInMainHand();
        FacelessItems plugin = context.getPlugin();

        // Contador de bloques rotos para el daño único
        int blocksBrokenCount = 0;

        for (Block currentBlock : blocksToProcess) {
            boolean wasReplanted = false;

            // --- Lógica de Replantación ---
            if (replantModifier != null) {
                EffectContext blockContext = new EffectContext(player, null, context.getBukkitEvent(), Map.of("broken_block", currentBlock), context.getItemKey(), plugin);
                if (replantModifier.getConditions().stream().allMatch(c -> c.check(blockContext)) && currentBlock.getBlockData() instanceof Ageable) {
                    // Modificado: breakAndReplant ya no llama a damageTool
                    breakAndReplant(currentBlock, player, tool, plugin);
                    wasReplanted = true;
                }
            }

            if (!wasReplanted) {
                if (triggerEvent) {
                    plugin.getItemEventListener().getAreaEffectUsers().add(player.getUniqueId());
                    boolean broken = player.breakBlock(currentBlock);
                    plugin.getItemEventListener().getAreaEffectUsers().remove(player.getUniqueId());

                    // ELIMINADO: if (broken) damageTool(player, tool); <--- Se daña una sola vez al final
                    if (broken) blocksBrokenCount++;
                } else {
                    // ELIMINADO: if (player.getGameMode() != GameMode.CREATIVE) damageTool(player, tool); <--- Se daña una sola vez al final
                    currentBlock.breakNaturally(tool);
                    blocksBrokenCount++;
                }
            } else {
                blocksBrokenCount++;
            }
        }

        // --- NUEVA LÓGICA DE DAÑO: DAÑO ÚNICO Y APLAZADO ---
        if (blocksBrokenCount > 0 && player.getGameMode() != GameMode.CREATIVE) {
            // Aplazamos el daño 1 tick para asegurar que todos los BlockDropItemEvent (Smelt) se hayan procesado.
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Re-obtenemos la herramienta para el caso de que haya cambiado de slot.
                    ItemStack finalTool = player.getInventory().getItemInMainHand();
                    if (finalTool != null && finalTool.equals(tool)) {
                        damageTool(player, finalTool);
                    }
                }
            }.runTaskLater(plugin, 1L);
        }
    }

    public List<Block> findBlocksToBreak(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return Collections.emptyList();

        Block startBlock = null;
        // ... (Lógica para encontrar startBlock, no la necesitamos cambiar) ...
        if (targetType == EffectTarget.BLOCK_IN_SIGHT) {
            RayTraceResult result = player.rayTraceBlocks(range);
            if (result != null && result.getHitBlock() != null) {
                startBlock = result.getHitBlock();
            }
        } else if (context.getData().get("broken_block") instanceof Block blockFromContext) {
            startBlock = blockFromContext;
        }

        if (startBlock == null) return Collections.emptyList();

        // ELIMINAMOS la verificación inicial del startBlock aquí.
        // Ahora, el filtro se aplica a todos los bloques, incluido el inicial.

        List<Block> blocksFound = new ArrayList<>();
        // ... (Lógica para determinar primaryFace, no la necesitamos cambiar) ...
        Vector direction = player.getEyeLocation().getDirection();
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
            Block layerCenterBlock = startBlock.getRelative(primaryFace, i);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block currentBlock;
                        if (primaryFace == BlockFace.UP || primaryFace == BlockFace.DOWN) {
                            currentBlock = layerCenterBlock.getRelative(x, 0, z);
                        } else if (primaryFace == BlockFace.EAST || primaryFace == BlockFace.WEST) {
                            currentBlock = layerCenterBlock.getRelative(0, y, z);
                        } else {
                            currentBlock = layerCenterBlock.getRelative(x, y, 0);
                        }

                        // --- CORRECCIÓN CLAVE: Aplicar el filtro de Materiales aquí ---
                        Material currentType = currentBlock.getType();

                        // Condición 1: Debe ser minable (dureza >= 0 y no aire)
                        if (currentType.getHardness() < 0 || currentType == Material.AIR) {
                            continue;
                        }

                        // Condición 2: Si la lista de filtrado NO está vacía, el tipo DEBE estar en ella.
                        if (!mineableBlocks.isEmpty() && !mineableBlocks.contains(currentType)) {
                            continue;
                        }

                        blocksFound.add(currentBlock);
                    }
                }
            }
        }
        return blocksFound;
    }

// BreakBlockEffect.java

    private void breakAndReplant(Block block, Player player, ItemStack tool, FacelessItems plugin) {
        Material cropType = block.getType();

        if (player.getGameMode() == GameMode.CREATIVE) {
            block.breakNaturally();
        } else {
            // ELIMINADO: damageTool(player, tool);
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

    private void damageTool(Player player, ItemStack tool) {
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (tool.getItemMeta() instanceof Damageable damageable) {
            int unbreakingLevel = tool.getEnchantmentLevel(Enchantment.UNBREAKING);
            if (Math.random() * 100 < (100.0 / (unbreakingLevel + 1))) {
                damageable.setDamage(damageable.getDamage() + 1);
                tool.setItemMeta(damageable);
                if (damageable.getDamage() >= tool.getType().getMaxDurability()) {
                    player.getInventory().setItemInMainHand(null);
                    player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
                }
            }
        }
    }

    @Override
    public String getType() {
        return "BREAK_BLOCK";
    }
}