package bw.development.facelessItems.Effects;

import bw.development.facelessItems.FacelessItems;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EffectContext {

    private final Player user;
    private final Entity targetEntity;
    private final Event bukkitEvent;
    private final Map<String, Object> data;
    private final String itemKey;
    private final FacelessItems plugin;

    public EffectContext(Player user, Entity targetEntity, Event bukkitEvent, Map<String, Object> data, String itemKey, FacelessItems plugin) {
        this.user = user;
        this.targetEntity = targetEntity;
        this.bukkitEvent = bukkitEvent;
        // --- LÍNEA CORREGIDA ---
        // Nos aseguramos de que el mapa sea siempre mutable creando un nuevo HashMap.
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.itemKey = itemKey;
        this.plugin = plugin;
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

    public String getItemKey() {
        return itemKey;
    }

    public FacelessItems getPlugin() {
        return plugin;
    }
}