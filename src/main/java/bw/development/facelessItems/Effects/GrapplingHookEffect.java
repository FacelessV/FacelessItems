package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.util.Vector;

import java.util.List;

// 1. Now extends BaseEffect
public class GrapplingHookEffect extends BaseEffect {

    private final double strength;

    // 2. The constructor now accepts the list of conditions
    public GrapplingHookEffect(double strength, List<Condition> conditions) {
        super(conditions); // 3. Pass conditions to the parent class
        this.strength = strength;
    }

    // 4. Renamed 'apply' to 'applyEffect'
    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        Event event = context.getBukkitEvent();

        if (player == null || !(event instanceof PlayerFishEvent fishEvent)) {
            return;
        }

        // The rest of your logic is perfect and remains unchanged
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