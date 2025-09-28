package bw.development.facelessItems.Commands;

import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Managers.MessageManager; // <-- AÑADIR IMPORT
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GiveItemCommand implements CommandExecutor {

    private final CustomItemManager customItemManager;
    private final MessageManager messageManager; // <-- AÑADIR CAMPO

    // --- CONSTRUCTOR ACTUALIZADO ---
    public GiveItemCommand(CustomItemManager customItemManager, MessageManager messageManager) {
        this.customItemManager = customItemManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Envolvemos al sender en un Player si es posible, para los mensajes
        Player playerSender = (sender instanceof Player) ? (Player) sender : null;

        if (!sender.hasPermission("facelessitems.giveitem")) {
            if (playerSender != null) messageManager.sendMessage(playerSender, "no_permission");
            else sender.sendMessage(messageManager.getMessage("no_permission"));
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /giveitem <player> <item>"); // Mensaje de uso puede quedar así
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null || !target.isOnline()) {
            if (playerSender != null) messageManager.sendMessage(playerSender, "player_not_found", "{player}", args[0]);
            else sender.sendMessage(messageManager.getMessage("player_not_found", "{player}", args[0]));
            return true;
        }

        String itemKey = args[1].toLowerCase();
        CustomItem customItem = customItemManager.getCustomItemByKey(itemKey);
        if (customItem == null) {
            if (playerSender != null) messageManager.sendMessage(playerSender, "item_not_found", "{item_key}", itemKey);
            else sender.sendMessage(messageManager.getMessage("item_not_found", "{item_key}", itemKey));
            return true;
        }

        String displayName = customItem.getItemStack().getItemMeta().getDisplayName();
        target.getInventory().addItem(customItem.getItemStack());

        if (playerSender != null) {
            messageManager.sendMessage(playerSender, "item_given_sender", "{item_name}", displayName, "{player}", target.getName());
        } else {
            // Mensaje para la consola
            sender.sendMessage(messageManager.getMessage("item_given_sender", "{item_name}", displayName, "{player}", target.getName()));
        }

        messageManager.sendMessage(target, "item_given_receiver", "{item_name}", displayName);

        return true;
    }
}