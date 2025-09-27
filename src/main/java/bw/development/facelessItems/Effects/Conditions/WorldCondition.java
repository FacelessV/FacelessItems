package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

public class WorldCondition implements Condition {

    private final Set<String> worldNames;
    private final boolean isBlacklist;

    public WorldCondition(Set<String> worldNames, boolean isBlacklist) {
        // Guardamos los nombres de los mundos en minúsculas para una comparación sin errores
        this.worldNames = worldNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        this.isBlacklist = isBlacklist;
    }

    @Override
    public boolean check(EffectContext context) {
        Player user = context.getUser();
        if (user == null) {
            // Si no hay un jugador, no podemos saber el mundo.
            // Devolvemos true para no bloquear efectos que no dependen de un jugador.
            return true;
        }

        String currentWorldName = user.getWorld().getName().toLowerCase();
        boolean matchFound = worldNames.contains(currentWorldName);

        // Si es lista negra, retorna true si NO hay coincidencia.
        // Si es lista blanca, retorna true si SÍ hay coincidencia.
        return isBlacklist != matchFound;
    }
}