package bw.development.facelessItems.Effects; // O donde prefieras guardarlo

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {

    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    /**
     * Inicia un cooldown para un jugador y una clave específica.
     * @param player El jugador.
     * @param key El identificador único del cooldown.
     * @param seconds La duración del cooldown en segundos.
     */
    public void setCooldown(Player player, String key, int seconds) {
        if (seconds <= 0) return;
        long expirationTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(seconds);
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(key, expirationTime);
    }

    /**
     * Comprueba si un jugador tiene un cooldown activo para una clave específica.
     * @param player El jugador.
     * @param key El identificador del cooldown.
     * @return true si está en cooldown, false en caso contrario.
     */
    public boolean isOnCooldown(Player player, String key) {
        return getRemainingCooldown(player, key) > 0;
    }

    /**
     * Obtiene el tiempo restante de un cooldown en milisegundos.
     * @param player El jugador.
     * @param key El identificador del cooldown.
     * @return El tiempo restante en milisegundos, o 0 si no está en cooldown.
     */
    public long getRemainingCooldown(Player player, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null || !playerCooldowns.containsKey(key)) {
            return 0;
        }

        long expirationTime = playerCooldowns.get(key);
        long remaining = expirationTime - System.currentTimeMillis();

        if (remaining <= 0) {
            playerCooldowns.remove(key); // Limpiar cooldowns expirados
            return 0;
        }
        return remaining;
    }
}