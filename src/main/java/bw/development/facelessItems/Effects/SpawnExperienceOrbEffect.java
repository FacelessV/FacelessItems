package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.Location;
import org.bukkit.entity.ExperienceOrb;
import java.util.List;

public class SpawnExperienceOrbEffect extends LocationBasedEffect {

    private final int amount;

    public SpawnExperienceOrbEffect(int amount, EffectTarget target, int range, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, range, conditions, cooldown, cooldownId);
        this.amount = amount;
    }

    @Override
    protected void applyAtLocation(Location location, EffectContext context) {
        if (location.getWorld() != null) {
            location.getWorld().spawn(location, ExperienceOrb.class, orb -> orb.setExperience(amount));
        }
    }

    @Override
    public String getType() {
        return "SPAWN_EXPERIENCE_ORB";
    }
}