package bw.development.facelessItems.Effects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Collections;
import java.util.Map;

/**
 * Contiene el contexto y los datos necesarios para aplicar un efecto.
 * Encapsula al usuario, la entidad objetivo, el evento de Bukkit, y datos adicionales.
 */
public class EffectContext {

    private final Player user;
    private final Entity targetEntity;
    private final Event bukkitEvent;
    private final Map<String, Object> data;

    public EffectContext(Player user, Entity targetEntity, Event bukkitEvent, Map<String, Object> data) {
        this.user = user;
        this.targetEntity = targetEntity;
        this.bukkitEvent = bukkitEvent;
        this.data = data != null ? data : Collections.emptyMap();
    }

    public Player getUser() {
        return user;
    }

    public Entity getTargetEntity() {
        return targetEntity;
    }

    public Event getBukkitEvent() {
        return bukkitEvent;
    }

    public Map<String, Object> getData() {
        return data;
    }
}