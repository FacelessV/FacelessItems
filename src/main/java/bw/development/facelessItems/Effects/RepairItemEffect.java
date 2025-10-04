package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.List;

public class RepairItemEffect extends BaseEffect {

    private final int repairAmount;

    public RepairItemEffect(int repairAmount, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.repairAmount = repairAmount;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.hasItemMeta() && item.getItemMeta() instanceof Damageable damageable) {

            int currentDamage = damageable.getDamage();

            // Aplicamos la reparación (restamos al daño actual)
            // Math.max(0, ...) asegura que el daño no sea negativo (durabilidad no sea infinita).
            int newDamage = Math.max(0, currentDamage - repairAmount);

            damageable.setDamage(newDamage);
            item.setItemMeta(damageable);

            // Opcional: Feedback visual de que el ítem fue reparado
            // player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_ANVIL_USE, 0.5f, 1.5f);
        }
    }

    @Override
    public String getType() {
        return "REPAIR_ITEM";
    }
}