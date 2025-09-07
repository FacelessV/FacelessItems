package bw.development.facelessItems.Effects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;

import java.util.Map;

public class MapConfigurationSection extends MemoryConfiguration {
    public MapConfigurationSection(String name, Map<?, ?> map) {
        super();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (entry.getKey() instanceof String key) {
                this.set(key, entry.getValue());
            }
        }
    }
}
