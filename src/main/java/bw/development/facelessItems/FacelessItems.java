package bw.development.facelessItems;

import bw.development.facelessItems.Commands.FacelessItemsCommand;
import bw.development.facelessItems.Commands.GiveItemCommand;
import bw.development.facelessItems.Commands.GiveItemTabCompleter;
import bw.development.facelessItems.Effects.ShadowCloneEffect;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Listeners.ItemEventListener;
import bw.development.facelessItems.Rarity.RarityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FacelessItems extends JavaPlugin {
    private static FacelessItems instance;
    private CustomItemManager customItemManager;
    private RarityManager rarityManager;

    @Override
    public void onEnable() {
        instance = this;
        try {
            this.rarityManager = new RarityManager(this);
            this.rarityManager.loadRarities();

            this.customItemManager = new CustomItemManager(this);
            this.customItemManager.loadItems();

            getServer().getPluginManager().registerEvents(new ItemEventListener(this, customItemManager), this);

            if (getCommand("giveitem") == null) {
                getLogger().severe("El comando 'giveitem' no está registrado en plugin.yml!");
            } else {
                getCommand("giveitem").setExecutor(new GiveItemCommand(this, customItemManager));
                this.getCommand("giveitem").setTabCompleter(new GiveItemTabCompleter(this, customItemManager));
            }

            if (getCommand("facelessitems") == null) {
                getLogger().severe("El comando 'facelessitems' no está registrado en plugin.yml!");
            } else {
                getCommand("facelessitems").setExecutor(new FacelessItemsCommand(this));
            }


            getLogger().info("FacelessItems habilitado con " + customItemManager.getItemCount() + " items cargados.");
        } catch (Exception e) {
            getLogger().severe("Error crítico en onEnable: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onDisable() {
        getLogger().info("FacelessItems deshabilitado.");
        ShadowCloneEffect.cleanUpClones();
    }

    public static FacelessItems getInstance() {
        return instance;
    }

    public CustomItemManager getCustomItemManager() {
        return customItemManager;
    }

    public RarityManager getRarityManager() {
        return rarityManager;
    }
}