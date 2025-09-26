package bw.development.facelessItems;

import bw.development.facelessItems.Commands.FacelessItemsCommand;
import bw.development.facelessItems.Commands.GiveItemCommand;
import bw.development.facelessItems.Commands.GiveItemTabCompleter;
import bw.development.facelessItems.Effects.CooldownManager; // 1. Import CooldownManager
import bw.development.facelessItems.Effects.ShadowCloneEffect;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Listeners.ItemEventListener;
import bw.development.facelessItems.Rarity.RarityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FacelessItems extends JavaPlugin {
    private static FacelessItems instance;
    private CustomItemManager customItemManager;
    private RarityManager rarityManager;
    private CooldownManager cooldownManager; // 2. Add CooldownManager field

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig(); // Good practice to ensure config.yml exists

        try {
            this.cooldownManager = new CooldownManager(); // 3. Initialize the manager
            this.rarityManager = new RarityManager(this);
            this.rarityManager.loadRarities();

            this.customItemManager = new CustomItemManager(this);
            this.customItemManager.loadItems();

            getServer().getPluginManager().registerEvents(new ItemEventListener(this, customItemManager), this);

            if (getCommand("giveitem") == null) {
                getLogger().severe("El comando 'giveitem' no está registrado en plugin.yml!");
            } else {
                getCommand("giveitem").setExecutor(new GiveItemCommand(customItemManager));
                getCommand("giveitem").setTabCompleter(new GiveItemTabCompleter(customItemManager));
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
            getServer().getPluginManager().disablePlugin(this); // Disable plugin on critical error
        }
    }

    @Override
    public void onDisable() {
        ShadowCloneEffect.cleanUpClones();
        getLogger().info("FacelessItems deshabilitado.");
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

    // 4. Add a getter for the CooldownManager
    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }
}