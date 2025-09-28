package bw.development.facelessItems.Commands;

import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Managers.MessageManager; // <-- AÑADIR IMPORT
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FacelessItemsCommand implements CommandExecutor {

    private final FacelessItems plugin;
    private final MessageManager messageManager; // <-- AÑADIR CAMPO

    // --- CONSTRUCTOR ACTUALIZADO ---
    public FacelessItemsCommand(FacelessItems plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            // Los mensajes de ayuda pueden quedar en el código, ya que no son mensajes de feedback
            sender.sendMessage("§eFacelessItems - Commands:");
            sender.sendMessage("§7/facelessitems reload§f - Reloads items from /items/");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("facelessitems.admin")) {
                if (player != null) messageManager.sendMessage(player, "no_permission");
                else sender.sendMessage(messageManager.getMessage("no_permission"));
                return true;
            }

            plugin.getRarityManager().loadRarities();
            plugin.getCustomItemManager().loadItems();

            if (player != null) messageManager.sendMessage(player, "reload_success");
            else sender.sendMessage(messageManager.getMessage("reload_success"));

            return true;
        }

        sender.sendMessage("§cUnknown subcommand. Use /facelessitems help");
        return true;
    }
}