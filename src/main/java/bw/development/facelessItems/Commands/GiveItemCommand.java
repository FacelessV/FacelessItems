package bw.development.facelessItems.Commands;

import bw.development.facelessItems.Items.CustomItem;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Managers.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;

public class GiveItemCommand implements CommandExecutor {

    private final CustomItemManager customItemManager;
    private final MessageManager messageManager;

    public GiveItemCommand(CustomItemManager customItemManager, MessageManager messageManager) {
        this.customItemManager = customItemManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player playerSender = (sender instanceof Player) ? (Player) sender : null;

        if (!sender.hasPermission("facelessitems.giveitem")) {
            if (playerSender != null) messageManager.sendMessage(playerSender, "no_permission");
            else sender.sendMessage(messageManager.getMessage("no_permission"));
            return true;
        }

        // Se requiere al menos /giveitem <player> <item>
        if (args.length < 2 || args.length > 3) {
            sender.sendMessage("§cUsage: /giveitem <player> <item> [amount]");
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

        // --- MANEJO DE CANTIDAD ---
        int amount = 1; // Valor por defecto si no se proporciona args[2]
        if (args.length == 3) {
            try {
                amount = Integer.parseInt(args[2]);
                if (amount <= 0) {
                    sender.sendMessage("§cThe amount must be a positive number.");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("§cInvalid amount: " + args[2] + ". Must be a number.");
                return true;
            }
        }

        // El método de la API para obtener el ítem con cantidad (que ya implementamos)
        // Nota: Asumo que tienes un método similar en CustomItemManager que acepta cantidad.
        ItemStack itemToGive = customItem.getItemStack().clone();
        itemToGive.setAmount(amount);

        String displayName = itemToGive.getItemMeta().getDisplayName();
        target.getInventory().addItem(itemToGive);

        // --- MENSAJES DE CONFIRMACIÓN ---

        String amountPlaceholder = String.valueOf(amount);

        if (playerSender != null) {
            messageManager.sendMessage(playerSender, "item_given_sender",
                    "{item_name}", displayName,
                    "{player}", target.getName(),
                    "{amount}", amountPlaceholder); // Nuevo placeholder para cantidad
        } else {
            // Mensaje para la consola
            sender.sendMessage(messageManager.getMessage("item_given_sender",
                    "{item_name}", displayName,
                    "{player}", target.getName(),
                    "{amount}", amountPlaceholder));
        }

        messageManager.sendMessage(target, "item_given_receiver",
                "{item_name}", displayName,
                "{amount}", amountPlaceholder); // Nuevo placeholder para cantidad

        return true;
    }
}