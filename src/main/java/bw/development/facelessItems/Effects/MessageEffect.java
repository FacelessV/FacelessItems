package bw.development.facelessItems.Effects;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class MessageEffect implements Effect {

    private final String message;

    public MessageEffect(String message) {
        // The color translation is done only once when the item is loaded.
        this.message = ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public void apply(EffectContext context) {
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