package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

import java.util.List;

public class GrapplingHookEffect extends BaseEffect {

    private final double strength;

    // --- CONSTRUCTOR UPDATED ---
    // Now accepts cooldown and cooldownId
    public GrapplingHookEffect(double strength, List<Condition> conditions, int cooldown, String cooldownId) {
        // And passes them to the parent class
        super(conditions, cooldown, cooldownId);
        this.strength = strength;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        Event event = context.getBukkitEvent();

        if (player == null || !(event instanceof PlayerFishEvent fishEvent)) {
            return;
        }

        // Your physics logic remains unchanged.
        if (fishEvent.getState() == PlayerFishEvent.State.IN_GROUND) {
            if (fishEvent.getHook() == null) {
                return;
            }

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