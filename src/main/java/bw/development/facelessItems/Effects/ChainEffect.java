package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChainEffect extends BaseEffect {

    // CAMBIO 1: La lista ahora es de 'BaseEffect' en lugar de 'Effect'
    private final List<BaseEffect> chainedEffects;
    private final int delayBetweenEffects;

    // CAMBIO 2: El constructor ahora acepta una lista de 'BaseEffect'
    public ChainEffect(List<BaseEffect> chainedEffects, int delayBetweenEffects, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.chainedEffects = chainedEffects;
        this.delayBetweenEffects = delayBetweenEffects;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        if (chainedEffects.isEmpty()) return;

        FacelessItems plugin = context.getPlugin();
        if (plugin == null) {
            if (context.getUser() != null) {
                context.getUser().sendMessage("§cError interno: Plugin no disponible para efectos en cadena.");
            }
            return;
        }

        AtomicInteger currentIndex = new AtomicInteger(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentIndex.get() >= chainedEffects.size()) {
                    this.cancel();
                    return;
                }

                // Ahora Java sabe que 'currentEffect' es un BaseEffect y tiene el método applyEffect()
                BaseEffect currentEffect = chainedEffects.get(currentIndex.get());

                // Esta línea ahora funciona sin errores
                currentEffect.applyEffect(context);

                currentIndex.incrementAndGet();

                if (currentIndex.get() >= chainedEffects.size()) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, delayBetweenEffects);
    }

    @Override
    public String getType() {
        return "CHAIN";
    }
}