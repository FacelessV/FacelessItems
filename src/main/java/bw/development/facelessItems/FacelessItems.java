package bw.development.facelessItems;

import bw.development.facelessItems.Commands.FacelessItemsCommand;
import bw.development.facelessItems.Commands.GiveItemCommand;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Listeners.ItemEventListener;
import bw.development.facelessItems.Rarity.RarityManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public class FacelessItems extends JavaPlugin {

    private CustomItemManager customItemManager;
    private RarityManager rarityManager;

    @Override
    public void onEnable() {
        try {
            this.rarityManager = new RarityManager(this);
            this.rarityManager.loadRarities();

            this.customItemManager = new CustomItemManager(this);
            this.customItemManager.loadItems();

            getServer().getPluginManager().registerEvents(new ItemEventListener(this), this);

            Objects.requireNonNull(getCommand("giveitem")).setExecutor(new GiveItemCommand(this));
            Objects.requireNonNull(getCommand("facelessitems")).setExecutor(new FacelessItemsCommand(this));

            getLogger().info("FacelessItems habilitado con " + customItemManager.getItemCount() + " items cargados.");
        } catch (Exception e) {
            getLogger().severe("Error crítico en onEnable: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onDisable() {
        getLogger().info("FacelessItems deshabilitado.");
    }

    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    public RarityManager getRarityManager() {
        return rarityManager;
    }
}
