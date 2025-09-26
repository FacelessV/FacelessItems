package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

// 1. Now extends BaseEffect
public class MessageEffect extends BaseEffect {

    private final String message;

    // 2. The constructor now accepts the list of conditions
    public MessageEffect(String message, List<Condition> conditions) {
        super(conditions); // 3. Pass conditions to the parent class
        this.message = ChatColor.translateAlternateColorCodes('&', message);
    }

    // 4. Renamed 'apply' to 'applyEffect'
    @Override
    protected void applyEffect(EffectContext context) {
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