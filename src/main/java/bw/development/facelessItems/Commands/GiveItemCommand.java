package bw.development.facelessItems.Commands;

import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveItemCommand implements CommandExecutor {

    private final FacelessItems plugin;

    public GiveItemCommand(FacelessItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!sender.hasPermission("facelessitems.giveitem")) {
            sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Uso correcto: /giveitem <jugador> <item>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "El jugador '" + args[0] + "' no está en línea.");
            return true;
        }

        String itemKey = args[1].toLowerCase();

        CustomItem customItem = plugin.getCustomItemManager().getCustomItemByKey(itemKey);
        if (customItem == null) {
            sender.sendMessage(ChatColor.RED + "El ítem '" + itemKey + "' no existe.");
            return true;
        }

        target.getInventory().addItem(customItem.getItemStack());
        sender.sendMessage(ChatColor.GREEN + "Has dado el ítem " + ChatColor.RESET + customItem.getItemStack().getItemMeta().getDisplayName() + ChatColor.GREEN + " a " + target.getName());
        target.sendMessage(ChatColor.GREEN + "Has recibido un ítem personalizado: " + customItem.getItemStack().getItemMeta().getDisplayName());

        return true;
    }
}
