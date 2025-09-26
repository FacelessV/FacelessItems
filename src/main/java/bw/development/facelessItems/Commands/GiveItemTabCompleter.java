package bw.development.facelessItems.Commands;

import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GiveItemTabCompleter implements TabCompleter {

    private final FacelessItems plugin;

    public GiveItemTabCompleter(FacelessItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            // Sugerir nombres de jugadores
            List<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getOnlinePlayers()) {
                playerNames.add(player.getName());
            }
            return playerNames;
        }

        if (args.length == 2) {
            // Sugerir las claves de los ítems personalizados
            return plugin.getCustomItemManager().getAllCustomItems().stream()
                    .map(CustomItem::getKey)
                    .collect(Collectors.toList());
        }

        return new ArrayList<>(); // No sugerir nada para otros argumentos
    }
}