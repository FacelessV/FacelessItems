package bw.development.facelessItems.Effects;

import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ChainEffect extends BaseEffect {

    private final List<BaseEffect> chainedEffects;
    private final int delayBetweenEffects;

    public ChainEffect(List<BaseEffect> chainedEffects, int delayBetweenEffects, List<Condition> conditions, int cooldown, String cooldownId) {
        super(conditions, cooldown, cooldownId);
        this.chainedEffects = chainedEffects;
        this.delayBetweenEffects = delayBetweenEffects;
    }

    /**
     * Obtiene la lista de efectos que componen esta cadena.
     * Este método es necesario para que el ItemEventListener pueda buscar
     * modificadores (como SmeltEffect) dentro de la cadena.
     */
    public List<BaseEffect> getChainedEffects() {
        return chainedEffects;
    }

    @Override
    protected void applyEffect(EffectContext context) {
        if (chainedEffects.isEmpty()) return;

        FacelessItems plugin = context.getPlugin();
        if (plugin == null) return;

        // --- PRE-CONFIGURATION FOR REPLANT ---
        // Find the Replant modifier in the chain.
        ReplantEffect replantModifier = chainedEffects.stream()
                .filter(ReplantEffect.class::isInstance)
                .map(ReplantEffect.class::cast)
                .findFirst().orElse(null);

        // If it exists, inject it into any mining effects in the chain.
        if (replantModifier != null) {
            for (BaseEffect effect : chainedEffects) {
                if (effect instanceof BreakBlockEffect breakBlockEffect) {
                    breakBlockEffect.setReplantModifier(replantModifier);
                } else if (effect instanceof VeinMineEffect veinMineEffect) {
                    veinMineEffect.setReplantModifier(replantModifier);
                }
            }
        }

        AtomicInteger currentIndex = new AtomicInteger(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentIndex.get() >= chainedEffects.size()) {
                    this.cancel();
                    return;
                }

                BaseEffect currentEffect = chainedEffects.get(currentIndex.get());

                // We don't execute modifier effects directly.
                if (!(currentEffect instanceof SmeltEffect) && !(currentEffect instanceof ReplantEffect)) {
                    currentEffect.applyEffect(context);
                }

                currentIndex.incrementAndGet();
            }
        }.runTaskTimer(plugin, 0, delayBetweenEffects);
    }

    @Override
    public String getType() {
        return "CHAIN";
    }
}