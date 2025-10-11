package bw.development.facelessItems.Sets;

import bw.development.facelessItems.Effects.BaseEffect;
import bw.development.facelessItems.Effects.Conditions.Condition;
import bw.development.facelessItems.Effects.DamageMultiplierEffect;
import bw.development.facelessItems.Effects.EffectContext;
import bw.development.facelessItems.FacelessItems;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect; // Importación de la clase de Bukkit
import java.util.*;

public class PassiveEffectApplier {

    private final FacelessItems plugin;

    public PassiveEffectApplier(FacelessItems plugin) {
        this.plugin = plugin;
    }

    public void applyEffects(Player player, List<BaseEffect> allPassiveEffects, Map<UUID, Set<org.bukkit.potion.PotionEffect>> activeSetEffects) {
        // effectsToApply ahora usa el tipo de Bukkit para ser compatible con el mapa y la API.
        Set<org.bukkit.potion.PotionEffect> effectsToApply = new HashSet<>();

        // Creamos el contexto una sola vez para las verificaciones.
        EffectContext context = new EffectContext(player, player, null, new HashMap<>(), "PASSIVE_CHECK", plugin);

        // 1. Procesar todos los efectos
        for (BaseEffect effect : allPassiveEffects) {

            // Verificamos las condiciones ANTES de decidir si aplicamos el efecto.
            List<Condition> conditions = effect.getConditions();

            boolean conditionsMet = conditions.isEmpty() || (conditions.stream().allMatch(c -> c.check(context)));

            if (conditionsMet) {

                // --- APLICACIÓN CRÍTICA DE POTIONS ---
                // Verifica si es tu clase custom PotionEffect
                if (effect instanceof bw.development.facelessItems.Effects.PotionEffect customPotionEffect) {

                    // CORRECCIÓN: Creamos el objeto org.bukkit.potion.PotionEffect de Bukkit
                    effectsToApply.add(new org.bukkit.potion.PotionEffect(
                            customPotionEffect.getPotionType(),
                            customPotionEffect.getDuration(),
                            customPotionEffect.getAmplifier(),
                            true, // Forzar la aplicación del efecto
                            false)
                    );
                }
                // Nota: Los efectos de modificador como DAMAGE_MULTIPLIER pasan el chequeo
                // pero se ignoran aquí (se manejan en el ItemEventListener, no por el scheduler).
            }
        }

        // 2. Remoción de efectos antiguos
        Set<org.bukkit.potion.PotionEffect> previouslyActive = activeSetEffects.getOrDefault(player.getUniqueId(), Collections.emptySet());
        for (org.bukkit.potion.PotionEffect oldEffect : previouslyActive) {
            // Si el efecto estaba activo antes, pero ya no está en la lista de efectos a aplicar, lo quitamos.
            if (!effectsToApply.contains(oldEffect)) {
                player.removePotionEffect(oldEffect.getType());
            }
        }

        // 3. Aplicar todos los efectos nuevos/actuales.
        for (org.bukkit.potion.PotionEffect newEffect : effectsToApply) {
            // Aplicación forzada, la solución más robusta para evitar bloqueos de Bukkit
            player.addPotionEffect(newEffect, true);
        }

        // 4. Actualizar el mapa de efectos activos.
        activeSetEffects.put(player.getUniqueId(), effectsToApply);
    }
}