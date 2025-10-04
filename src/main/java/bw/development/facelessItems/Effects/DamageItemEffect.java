package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;

import java.util.List;

public class DamageItemEffect extends BaseEffect {

    private final int damageAmount;

    public DamageItemEffect(int damageAmount, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.damageAmount = damageAmount;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null || player.getGameMode().name().contains("CREATIVE")) return;

        // Asumimos que el efecto se aplica a la herramienta en mano (principal).
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.hasItemMeta() && item.getItemMeta() instanceof Damageable damageable) {

            int currentDamage = damageable.getDamage();
            int maxDurability = item.getType().getMaxDurability();

            // Aplicamos el daño, asegurándonos de no exceder la durabilidad máxima.
            int newDamage = Math.min(currentDamage + damageAmount, maxDurability);

            damageable.setDamage(newDamage);
            item.setItemMeta(damageable);

            // Si el ítem se rompe por el daño
            if (newDamage >= maxDurability) {
                // Lógica de rotura (similar a damageTool, pero sin chequeo de Unbreaking)
                player.getInventory().setItemInMainHand(null);
                player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public String getType() {
        return "DAMAGE_ITEM";
    }
}