package bw.development.facelessItems.Effects.Conditions;

import bw.development.facelessItems.Effects.EffectContext;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WeatherCondition implements Condition {

    public enum WeatherType {
        CLEAR,
        RAINY,
        THUNDERING
    }

    private final WeatherType requiredWeather;

    public WeatherCondition(WeatherType requiredWeather) {
        this.requiredWeather = requiredWeather;
    }

    @Override
    public boolean check(EffectContext context) {
        if (!(context.getUser() instanceof Player player)) {
            return true;
        }

        World world = player.getWorld();

        return switch (requiredWeather) {
            case CLEAR -> !world.hasStorm() && !world.isThundering();
            case RAINY -> world.hasStorm() && !world.isThundering();
            case THUNDERING -> world.isThundering();
        };
    }
}