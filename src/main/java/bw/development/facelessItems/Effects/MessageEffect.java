package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageEffect extends BaseEffect {

    private final String message;

    // --- CONSTRUCTOR UPDATED ---
    // Now accepts cooldown and cooldownId
    public MessageEffect(String message, List<Condition> conditions, int cooldown, String cooldownId) {
        // And passes them to the parent class
        super(conditions, cooldown, cooldownId);
        this.message = ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    protected void applyEffect(EffectContext context) {
        // This logic is perfect and needs no changes.
        Player user = context.getUser();
        if (user != null) {
            user.sendMessage(message);
        }
    }

    @Override
    public String getType() {
        return "MESSAGE";
    }
}