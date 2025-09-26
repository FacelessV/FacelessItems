package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

// 1. Now extends BaseEffect
public class ShadowCloneEffect extends BaseEffect {

    private static final Set<UUID> activeClones = new HashSet<>();
    private final int duration;
    private final double range;
    private final Particle particleType;
    private final Sound soundEffect;

    // 2. The constructor now accepts the list of conditions
    public ShadowCloneEffect(int duration, double range, Particle particleType, Sound soundEffect, List<Condition> conditions) {
        super(conditions); // 3. Pass conditions to the parent class
        this.duration = duration;
        this.range = range;
        this.particleType = particleType;
        this.soundEffect = soundEffect;
    }

    // 4. Renamed 'apply' to 'applyEffect'
    @Override
    protected void applyEffect(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        // --- Your excellent logic remains unchanged ---
        Location cloneLocation = player.getLocation();

        Villager clone = player.getWorld().spawn(cloneLocation, Villager.class);
        clone.setAI(true);
        clone.setInvulnerable(false);
        clone.getAttribute(Attribute.MAX_HEALTH).setBaseValue(100.0);
        clone.setHealth(100.0);

        clone.setCustomName(player.getName());
        clone.setCustomNameVisible(true);
        clone.getWorld().playSound(cloneLocation, soundEffect, 1.0f, 1.0f);

        activeClones.add(clone.getUniqueId());

        List<Entity> nearbyEntities = player.getNearbyEntities(range, range, range);
        for (Entity entity : nearbyEntities) {
            if (entity instanceof LivingEntity && entity instanceof org.bukkit.entity.Mob) {
                ((org.bukkit.entity.Mob) entity).setTarget(clone);
            }
        }

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (!clone.isValid()) {
                    activeClones.remove(clone.getUniqueId());
                    this.cancel();
                    return;
                }

                if (ticks < duration) {
                    player.getWorld().spawnParticle(particleType, clone.getLocation().add(0, 1, 0), 10, 0.5, 0.5, 0.5, 0);
                    ticks += 10;
                } else {
                    clone.remove();
                    activeClones.remove(clone.getUniqueId());
                    this.cancel();
                }
            }
        }.runTaskTimer(FacelessItems.getInstance(), 0, 10);
    }

    public static void cleanUpClones() {
        for (UUID cloneId : new HashSet<>(activeClones)) {
            Entity clone = FacelessItems.getInstance().getServer().getEntity(cloneId);
            if (clone != null && clone.isValid()) {
                clone.remove();
            }
        }
        activeClones.clear();
    }

    @Override
    public String getType() {
        return "SHADOW_CLONE";
    }
}