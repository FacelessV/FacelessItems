package bw.development.facelessItems.Managers;

import bw.development.facelessItems.FacelessItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class MessageManager {

    private final FacelessItems plugin;
    private FileConfiguration messagesConfig;
    private final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public MessageManager(FacelessItems plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        plugin.getLogger().info("Archivo messages.yml cargado.");
    }

    /**
     * Obtiene un Component de la configuración, aplicando placeholders.
     * @param key La clave del mensaje en messages.yml.
     * @param placeholders Un array de pares de strings para reemplazar (ej: "{player}", "Steve").
     * @return El Component formateado.
     */
    public Component getMessage(String key, String... placeholders) {
        String message = messagesConfig.getString(key, "&cMensaje no encontrado: " + key);

        // --- LÓGICA DE PLACEHOLDERS COMPLETADA ---
        // Itera sobre el array de placeholders de dos en dos.
        for (int i = 0; i < placeholders.length; i += 2) {
            // Asegurarse de que hay un valor para cada clave de placeholder
            if (i + 1 < placeholders.length) {
                // placeholders[i] es la clave (ej: "{player}")
                // placeholders[i+1] es el valor (ej: "Notch")
                message = message.replace(placeholders[i], placeholders[i + 1]);
            }
        }

        // Convierte el string final (con placeholders reemplazados) a un Component
        return serializer.deserialize(message);
    }

    /**
     * Envía un mensaje formateado a un jugador, incluyendo el prefijo.
     * @param player El jugador que recibirá el mensaje.
     * @param key La clave del mensaje.
     * @param placeholders Los placeholders a reemplazar.
     */
    public void sendMessage(Player player, String key, String... placeholders) {
        Component prefix = serializer.deserialize(messagesConfig.getString("prefix", ""));
        Component message = getMessage(key, placeholders);

        // El método moderno player.sendMessage() ya acepta Components.
        // .append() une el prefijo y el mensaje.
        player.sendMessage(prefix.append(message));
    }
}