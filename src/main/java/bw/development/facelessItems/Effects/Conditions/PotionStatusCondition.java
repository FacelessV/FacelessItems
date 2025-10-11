package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PotionStatusCondition implements Condition { // <-- Nuevo nombre más general

    private final Set<PotionEffectType> requiredEffects;
    private final boolean checkUser; // TRUE si chequea al jugador, FALSE si chequea al objetivo

    public PotionStatusCondition(List<String> effects, boolean checkUser) {
        this.requiredEffects = effects.stream()
                .map(s -> PotionEffectType.getByName(s.toUpperCase()))
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());
        this.checkUser = checkUser;
    }

    @Override
    public boolean check(EffectContext context) {

        // 1. Determinar la entidad a chequear (Subject)
        org.bukkit.entity.Entity rawSubject = checkUser ? context.getUser() : context.getTargetEntity();

        // Si el sujeto es nulo (ej. si TargetEntity es nulo en un on_hit fallido), o no es un LivingEntity, salimos.
        if (!(rawSubject instanceof LivingEntity subject)) {
            return false;
        }

        // 2. Comprobación de Seguridad Adicional para el USUARIO:
        // Si estamos chequeando al usuario (checkUser=true), el sujeto debe ser un Player.
        // Aunque Player extiende LivingEntity, esta comprobación es buena si el contexto permite otros tipos.
        if (checkUser && !(subject instanceof Player)) {
            return false;
        }

        // --- Lógica de la Condición ---
        // 3. La condición se cumple si el sujeto tiene AL MENOS UNO de los efectos requeridos.
        return requiredEffects.stream()
                .anyMatch(subject::hasPotionEffect);
    }
}