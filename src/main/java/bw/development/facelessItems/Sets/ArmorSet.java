package bw.development.facelessItems.Sets;

import java.util.Map;
import java.util.Set;

public class ArmorSet {
    private final String key;
    private final String displayName;
    private final Set<String> itemKeys;
    private final Map<Integer, ArmorSetBonus> bonuses;

    public ArmorSet(String key, String displayName, Set<String> itemKeys, Map<Integer, ArmorSetBonus> bonuses) {
        this.key = key;
        this.displayName = displayName;
        this.itemKeys = itemKeys;
        this.bonuses = bonuses;
    }

    public boolean containsItem(String itemKey) {
        return itemKeys.contains(itemKey);
    }

    public ArmorSetBonus getBonus(int pieceCount) {
        return bonuses.get(pieceCount);
    }

    // Getters for other fields if you need them
    public String getKey() {
        return key;
    }

    public Set<String> getItemKeys() {
        return itemKeys;
    }
}