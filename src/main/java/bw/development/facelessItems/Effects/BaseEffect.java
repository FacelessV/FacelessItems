package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.List;

public abstract class BaseEffect implements Effect {

    protected final List<Condition> conditions;
    protected final int cooldown;
    protected final String cooldownId;

    public BaseEffect(List<Condition> conditions, int cooldown, String cooldownId) {
        this.conditions = conditions;
        this.cooldown = cooldown;
        this.cooldownId = cooldownId;
    }

    @Override
    public final void apply(EffectContext context) {
        // Primero, comprobamos las condiciones. Si no se cumplen, el método termina silenciosamente.
        for (Condition condition : conditions) {
            if (!condition.check(context)) {
                return;
            }
        }

        // Si las condiciones pasan, continuamos.
        Player user = context.getUser();
        if (user == null) {
            // Si no hay jugador, no hay cooldowns, así que aplicamos el efecto.
            applyEffect(context);
            return;
        }

        // Después, si las condiciones pasan, comprobamos el cooldown.
        String finalCooldownId = (cooldownId != null && !cooldownId.isEmpty()) ? cooldownId : context.getItemKey();
        CooldownManager cooldownManager = context.getPlugin().getCooldownManager();

        if (cooldown > 0 && cooldownManager.isOnCooldown(user, finalCooldownId)) {
            long remainingMillis = cooldownManager.getRemainingCooldown(user, finalCooldownId);
            double remainingSeconds = remainingMillis / 1000.0;

            // Usamos el nuevo MessageManager para enviar el mensaje de cooldown
            String formattedSeconds = String.format("%.1f", remainingSeconds);
            context.getPlugin().getMessageManager().sendMessage(user, "item_on_cooldown", "{cooldown_remaining}", formattedSeconds);
            return;
        }

        // Si todo pasa, aplicamos el cooldown y el efecto.
        if (cooldown > 0) {
            cooldownManager.setCooldown(user, finalCooldownId, cooldown);
        }
        applyEffect(context);
    }

    /**
     * Contiene la lógica específica del efecto que se ejecutará si todas las condiciones se cumplen.
     * @param context El contexto del efecto.
     */
    protected abstract void applyEffect(EffectContext context);

    @Override
    public abstract String getType();

    public int getCooldown() {
        return cooldown;
    }

    public String getCooldownId() {
        return cooldownId;
    }

    public List<Condition> getConditions() {
        return conditions;
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
}