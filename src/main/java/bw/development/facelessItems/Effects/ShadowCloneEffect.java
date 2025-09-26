package bw.development.facelessItems.Effects;

import bw.development.facelessItems.FacelessItems;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.bukkit.attribute.Attribute;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ShadowCloneEffect implements Effect {

    private static final Set<UUID> activeClones = new HashSet<>();
    private final int duration;
    private final double range;
    private final Particle particleType;
    private final Sound soundEffect;

    public ShadowCloneEffect(int duration, double range, Particle particleType, Sound soundEffect) {
        this.duration = duration;
        this.range = range;
        this.particleType = particleType;
        this.soundEffect = soundEffect;
    }

    @Override
    public void apply(EffectContext context) {
        Player player = context.getUser();
        if (player == null) return;

        Location cloneLocation = player.getLocation();

        Villager clone = (Villager) player.getWorld().spawn(cloneLocation, Villager.class);
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