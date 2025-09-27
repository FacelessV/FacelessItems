package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.Event;

import java.util.List;

public class CureZombificationEffect extends TargetedEffect {

    private final int cureDuration;

    public CureZombificationEffect(int cureDuration, EffectTarget target, List<Condition> conditions, int cooldown, String cooldownId) {
        super(target, conditions, cooldown, cooldownId);
        this.cureDuration = cureDuration;
    }

    @Override
    protected void applyToTarget(LivingEntity target, Player user, Event event, EffectContext context) {
        if (!(target instanceof ZombieVillager zombieVillager)) {
            return;
        }

        if (zombieVillager.isConverting()) {
            return;
        }

        // --- LÓGICA DE CURACIÓN CORREGIDA SEGÚN TU DOCUMENTACIÓN ---

        // Paso 1: Establecemos al jugador que realiza la cura para que obtenga los descuentos.
        // El método pide un OfflinePlayer, pero un Player también funciona.
        zombieVillager.setConversionPlayer(user);

        // Paso 2: Establecemos el tiempo de conversión para iniciar el proceso.
        zombieVillager.setConversionTime(cureDuration);
    }

    @Override
    public String getType() {
        return "CURE_ZOMBIFICATION";
    }
}