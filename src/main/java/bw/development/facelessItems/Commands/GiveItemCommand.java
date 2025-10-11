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
import org.bukkit.inventory.PlayerInventory; // Importación necesaria
import java.util.Collections;
import java.util.HashMap; // Importación necesaria

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
        int amount = 1;
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

        ItemStack itemToGive = customItem.getItemStack().clone();
        itemToGive.setAmount(amount);
        String displayName = itemToGive.getItemMeta().getDisplayName();
        String amountPlaceholder = String.valueOf(amount);

        // =========================================================================
        // --- LÓGICA DE ENTREGA GARANTIZADA (INVENTARIO -> SUELO) ---
        // =========================================================================

        // 1. Intentar añadir el ítem. El mapa contiene los ítems que NO CABEN.
        HashMap<Integer, ItemStack> remaining = target.getInventory().addItem(itemToGive);

        // 2. Si el mapa NO está vacío, significa que el inventario estaba lleno y debemos soltar el excedente.
        if (!remaining.isEmpty()) {

            // Recorremos los ítems que no cupieron.
            for (ItemStack itemLeft : remaining.values()) {
                // Soltamos el ítem al suelo en la ubicación del jugador
                target.getWorld().dropItemNaturally(target.getLocation(), itemLeft);
            }

            // Opcional: Mensaje de alerta al jugador que recibe el ítem
            messageManager.sendMessage(target, "item_given_receiver_spilled",
                    "{item_name}", displayName,
                    "{amount}", String.valueOf(itemToGive.getAmount()));
        }

        // --- MENSAJES DE CONFIRMACIÓN AL SENDER (CONSOLA/ADMIN) ---
        if (playerSender != null) {
            messageManager.sendMessage(playerSender, "item_given_sender",
                    "{item_name}", displayName,
                    "{player}", target.getName(),
                    "{amount}", amountPlaceholder);
        } else {
            sender.sendMessage(messageManager.getMessage("item_given_sender",
                    "{item_name}", displayName,
                    "{player}", target.getName(),
                    "{amount}", amountPlaceholder));
        }

        // Solo enviamos el mensaje de "recibido" al target si el inventario NO estaba lleno, o si se envió al suelo.
        // Como la acción ya se manejó arriba (soltando al suelo), solo enviamos el mensaje simple de recepción
        // si el inventario NO estaba lleno, o si el mapa 'remaining' está vacío (lo que implica que todo entró).
        if (remaining.isEmpty()) {
            messageManager.sendMessage(target, "item_given_receiver",
                    "{item_name}", displayName,
                    "{amount}", amountPlaceholder);
        }

        return true;
    }
}