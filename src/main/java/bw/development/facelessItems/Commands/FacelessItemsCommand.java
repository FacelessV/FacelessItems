package bw.development.facelessItems.Commands;

import bw.development.facelessItems.FacelessItems;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Rarity.RarityManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;

public class FacelessItemsCommand implements CommandExecutor {

    private final FacelessItems plugin;

    public FacelessItemsCommand(FacelessItems plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sender.sendMessage(ChatColor.YELLOW + "FacelessItems - Comandos:");
            sender.sendMessage(ChatColor.GRAY + "/facelessitems reload" + ChatColor.WHITE + " - Recarga los ítems desde /items/");
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("facelessitems.admin")) {
                sender.sendMessage(ChatColor.RED + "No tienes permiso para usar este comando.");
                return true;
            }

            RarityManager rarityManager = plugin.getRarityManager();
            rarityManager.loadRarities();
            sender.sendMessage(ChatColor.GREEN + "¡Rarezas recargadas exitosamente!");

            CustomItemManager itemManager = plugin.getCustomItemManager();
            itemManager.loadItems();
            sender.sendMessage(ChatColor.GREEN + "¡Ítems recargados exitosamente!");

            return true;
        }

        sender.sendMessage(ChatColor.RED + "Subcomando desconocido. Usa /facelessitems help");
        return true;
    }
}
