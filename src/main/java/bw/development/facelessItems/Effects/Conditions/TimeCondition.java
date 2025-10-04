package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class TimeCondition implements Condition {

    private final boolean mustBeDay; // If true, checks for day. If false, checks for night.

    public TimeCondition(boolean mustBeDay) {
        this.mustBeDay = mustBeDay;
    }

    @Override
    public boolean check(EffectContext context) {
        Player user = context.getUser();
        if (user == null) {
            // As you correctly noted, the condition passes safely if we cannot identify the user.
            return true;
        }

        World world = user.getWorld();
        long time = world.getTime();

        // Minecraft Day is tick range [0, 12000)
        boolean isCurrentlyDay = time >= 0 && time < 12000;

        // --- CHECK LOGIC ---

        if (mustBeDay) {
            // If the configuration requires it to be day (e.g., is_day: true),
            // we check if it IS currently day.
            return isCurrentlyDay;
        } else {
            // If the configuration requires it to be night (e.g., is_day: false),
            // we check if it IS NOT currently day (i.e., it is night).
            return !isCurrentlyDay;
        }
    }
}