package bw.development.facelessItems.Effects;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.ChatColor;

public class MessageEffect implements Effect {

    private final String message;

    public MessageEffect(String message) {
        this.message = ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public void apply(Player player, Event event) {
        player.sendMessage(message);
    }
}
