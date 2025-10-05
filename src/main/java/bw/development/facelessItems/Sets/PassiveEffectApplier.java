package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.Effects.DamageMultiplierEffect; // Necesario para el chequeo
import bw.development.facelessItems.Effects.EffectContext;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import java.util.*;

public class PassiveEffectApplier {

    private final FacelessItems plugin;

    public PassiveEffectApplier(FacelessItems plugin) {
        this.plugin = plugin;
    }

    public void applyEffects(Player player, List<BaseEffect> allPassiveEffects, Map<UUID, Set<PotionEffect>> activeSetEffects) {
        Set<PotionEffect> effectsToApply = new HashSet<>();

        // Creamos el contexto una sola vez para las verificaciones.
        EffectContext context = new EffectContext(player, player, null, new HashMap<>(), "PASSIVE_CHECK", plugin);

        // DEBUG E: Verificar si la lista no está vacía al inicio del Applier
        //plugin.getLogger().info("DEBUG APPLIER: Procesando " + allPassiveEffects.size() + " efectos para " + player.getName());

        // 1. Procesar todos los efectos
        for (BaseEffect effect : allPassiveEffects) {

            // Verificamos las condiciones ANTES de decidir si aplicamos el efecto.
            List<Condition> conditions = effect.getConditions();

            // La condición es TRUE si la lista está vacía (no hay condiciones en el YAML).
            boolean conditionsMet = conditions.isEmpty() || (conditions.stream().allMatch(c -> c.check(context)));

            if (conditionsMet) {
                // DEBUG F: La condición fue TRUE.
                //plugin.getLogger().info("DEBUG APPLIER: Condición OK para: " + effect.getType());

                // --- APLICACIÓN CRÍTICA DE POTIONS ---
                if (effect instanceof bw.development.facelessItems.Effects.PotionEffect potionEffect) {

                    effectsToApply.add(new PotionEffect(
                            potionEffect.getPotionType(),
                            potionEffect.getDuration(),
                            potionEffect.getAmplifier(),
                            true, // Forzar la aplicación del efecto
                            false)
                    );
                }
                // Nota: Los efectos de modificador como DAMAGE_MULTIPLIER se ignoran aquí, pero pasan el chequeo.
            }
        }

        Set<PotionEffect> previouslyActive = activeSetEffects.getOrDefault(player.getUniqueId(), Collections.emptySet());
        for (PotionEffect oldEffect : previouslyActive) {
            if (!effectsToApply.contains(oldEffect)) {
                player.removePotionEffect(oldEffect.getType());
            }
        }

        // DEBUG H: Total de pociones finales a aplicar
       // plugin.getLogger().info("DEBUG APPLIER: Total de pociones a aplicar: " + effectsToApply.size());

        // 3. Aplicar todos los efectos nuevos/actuales.
        for (PotionEffect newEffect : effectsToApply) {
            // Aplicación forzada, la solución más robusta para evitar bloqueos de Bukkit
            player.addPotionEffect(newEffect, true);
        }

        // 4. Actualizar el mapa de efectos activos.
        activeSetEffects.put(player.getUniqueId(), effectsToApply);
    }
}