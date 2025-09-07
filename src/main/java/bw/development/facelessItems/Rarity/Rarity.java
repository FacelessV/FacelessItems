package bw.development.facelessItems.Rarity;

public class Rarity {

    private final String id;
    private final String name;
    private final String color;
    private final String loreTag;

    public Rarity(String id, String name, String color, String loreTag) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.loreTag = loreTag;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public String getLoreTag() {
        return loreTag;
    }
}
