package bw.development.facelessItems.Effects;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

public class GrapplingHookEffect implements Effect {

    private final double strength;

    public GrapplingHookEffect(double strength) {
        this.strength = strength;
    }

    @Override
    public void apply(EffectContext context) {
        Player player = context.getUser();
        Event event = context.getBukkitEvent();

        if (player == null || !(event instanceof PlayerFishEvent)) return;

        PlayerFishEvent fishEvent = (PlayerFishEvent) event;

        // Verificamos si el gancho está enganchado a un bloque
        if (fishEvent.getState() == PlayerFishEvent.State.IN_GROUND) {

            // Verificamos que el gancho exista para evitar un NullPointerException
            if (fishEvent.getHook() == null) return;

            Location hookLocation = fishEvent.getHook().getLocation();

            Vector direction = hookLocation.toVector().subtract(player.getLocation().toVector()).normalize();
            Vector velocity = direction.multiply(strength);

            player.setVelocity(velocity);
        }
    }

    @Override
    public String getType() {
        return "GRAPPLING_HOOK";
    }
}