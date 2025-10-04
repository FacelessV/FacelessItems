package bw.development.facelessItems.Commands;

import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Items.CustomItemManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class GiveItemTabCompleter implements TabCompleter {

    private final CustomItemManager customItemManager;

    // CONSTRUCTOR
    public GiveItemTabCompleter(CustomItemManager customItemManager) {
        this.customItemManager = customItemManager;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // ARG 1: Nombres de jugadores en línea
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }

        } else if (args.length == 2) {
            // ARG 2: Claves de ítems custom
            completions.addAll(customItemManager.getAllCustomItems().stream()
                    .map(CustomItem::getKey)
                    .collect(Collectors.toList()));

        } else if (args.length == 3) {
            // ARG 3: Cantidades sugeridas (por defecto 1, 16, 64)
            completions.addAll(Arrays.asList("1", "16", "64"));
        }

        // Filtramos las sugerencias para que coincidan con lo que el usuario ha escrito
        String currentArg = args[args.length - 1].toLowerCase();

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(currentArg))
                .collect(Collectors.toList());
    }
}