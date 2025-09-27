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
            return true; // Cannot determine time without a player's world
        }

        World world = user.getWorld();
        long time = world.getTime();

        // In Minecraft, day is roughly 0-12000 ticks, night is 12000-24000
        boolean isCurrentlyDay = time >= 0 && time < 12000;

        // If the condition requires it to be day, we return true if it is currently day.
        // If the condition requires it to be night (mustBeDay = false), we return true if it is currently NOT day.
        return isCurrentlyDay == mustBeDay;
    }
}