package bw.development.facelessItems.Managers; // O el paquete que prefieras

import bw.development.facelessItems.FacelessItems;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class MessageManager {

    private final FacelessItems plugin;
    private FileConfiguration messagesConfig;

    public MessageManager(FacelessItems plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    /**
     * Carga el archivo messages.yml desde el disco.
     */
    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            // Si no existe, lo crea desde los recursos del plugin
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Archivo messages.yml cargado.");
    }

    /**
     * Obtiene un mensaje de la configuración, aplica placeholders y códigos de color.
     * @param key La clave del mensaje en messages.yml (ej: "item_on_cooldown").
     * @param placeholders Un array de pares de strings para reemplazar (ej: "{player}", "Steve").
     * @return El mensaje formateado.
     */
    public String getMessage(String key, String... placeholders) {
        String message = messagesConfig.getString(key, "&cMensaje no encontrado: " + key);

        // Reemplaza los placeholders
        for (int i = 0; i < placeholders.length; i += 2) {
            if (i + 1 < placeholders.length) {
                message = message.replace(placeholders[i], placeholders[i+1]);
            }
        }

        // Aplica colores
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Envía un mensaje formateado a un jugador.
     * @param player El jugador que recibirá el mensaje.
     * @param key La clave del mensaje.
     * @param placeholders Los placeholders a reemplazar.
     */
    public void sendMessage(Player player, String key, String... placeholders) {
        String prefix = messagesConfig.getString("prefix", ""); // Obtiene el prefijo
        String message = getMessage(key, placeholders);

        // Combina prefijo y mensaje, y lo envía
        player.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}