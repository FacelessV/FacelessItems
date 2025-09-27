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

    @Override
    protected void applyEffect(EffectContext context) {
        if (chainedEffects.isEmpty()) return;

        FacelessItems plugin = context.getPlugin();
        if (plugin == null) return;

        // --- ¡NUEVA LÓGICA DE PRE-CONFIGURACIÓN! ---
        // 1. Buscamos si existe un modificador Smelt en la cadena.
        SmeltEffect smeltModifier = chainedEffects.stream()
                .filter(SmeltEffect.class::isInstance)
                .map(SmeltEffect.class::cast)
                .findFirst().orElse(null);

        // 2. Si existe, se lo asignamos a todos los efectos de minería en la cadena.
        if (smeltModifier != null) {
            for (BaseEffect effect : chainedEffects) {
                if (effect instanceof BreakBlockEffect breakBlockEffect) {
                    breakBlockEffect.setSmeltModifier(smeltModifier);
                } else if (effect instanceof VeinMineEffect veinMineEffect) {
                    veinMineEffect.setSmeltModifier(smeltModifier);
                }
            }
        }
        // --- FIN DE LA NUEVA LÓGICA ---

        AtomicInteger currentIndex = new AtomicInteger(0);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (currentIndex.get() >= chainedEffects.size()) {
                    this.cancel();
                    return;
                }

                BaseEffect currentEffect = chainedEffects.get(currentIndex.get());

                // No ejecutamos el SmeltEffect directamente, ya que solo es un modificador
                if (!(currentEffect instanceof SmeltEffect)) {
                    currentEffect.applyEffect(context);
                }

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