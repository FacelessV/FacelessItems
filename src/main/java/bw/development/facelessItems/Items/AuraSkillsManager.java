package bw.development.facelessItems.Items;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.AuraSkillsBukkit;
import dev.aurelium.auraskills.api.item.ItemManager;
import dev.aurelium.auraskills.api.item.ModifierType;
import dev.aurelium.auraskills.api.stat.Stat;
import dev.aurelium.auraskills.api.registry.GlobalRegistry;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import org.bukkit.inventory.ItemStack;
import java.util.List;
import java.util.Map;
import static dev.aurelium.auraskills.api.util.AuraSkillsModifier.Operation.ADD; // <-- ¡ESTA ES LA LÍNEA CLAVE!

public class AuraSkillsManager {

    private final AuraSkillsBukkit auraSkillsBukkit;
    private final AuraSkillsApi auraSkillsApi;

    public AuraSkillsManager() {
        this.auraSkillsBukkit = AuraSkillsBukkit.get();
        this.auraSkillsApi = AuraSkillsApi.get();
    }

    public ItemStack applyStatsToItem(ItemStack item, List<Map<String, Object>> auraSkillsStats, String itemKey) {
        if (auraSkillsBukkit == null || auraSkillsApi == null || auraSkillsStats == null || auraSkillsStats.isEmpty()) {
            return item;
        }

        ItemManager itemManager = auraSkillsBukkit.getItemManager();
        GlobalRegistry registry = auraSkillsApi.getGlobalRegistry();

        ItemStack modifiedItem = item.clone();

        for (Map<String, Object> statBoost : auraSkillsStats) {
            String statName = (String) statBoost.get("stat");
            Object amountObj = statBoost.get("amount");

            Stat stat = registry.getStat(NamespacedId.of("auraskills", statName.toLowerCase()));
            if (stat != null && amountObj instanceof Number) {
                modifiedItem = itemManager.addStatModifier(
                        modifiedItem,
                        ModifierType.ITEM,
                        stat,
                        ((Number) amountObj).doubleValue(),
                        ADD,
                        false
                );
            }
        }

        return modifiedItem;
    }
}