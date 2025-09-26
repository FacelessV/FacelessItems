package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
        Player user = context.getUser();
        if (user == null) {
            applyEffect(context);
            return;
        }

        String finalCooldownId = (cooldownId != null && !cooldownId.isEmpty()) ? cooldownId : context.getItemKey();
        CooldownManager cooldownManager = context.getPlugin().getCooldownManager();

        if (cooldown > 0 && cooldownManager.isOnCooldown(user, finalCooldownId)) {
            long remainingMillis = cooldownManager.getRemainingCooldown(user, finalCooldownId);
            double remainingSeconds = remainingMillis / 1000.0;

            user.sendMessage(ChatColor.RED + String.format("Puedes usar esta habilidad de nuevo en %.1fs.", remainingSeconds));
            return;
        }

        for (Condition condition : conditions) {
            if (!condition.check(context)) {
                return;
            }
        }

        if (cooldown > 0) {
            cooldownManager.setCooldown(user, finalCooldownId, cooldown);
        }
        applyEffect(context);
    }

    protected abstract void applyEffect(EffectContext context);

    @Override
    public abstract String getType();
}