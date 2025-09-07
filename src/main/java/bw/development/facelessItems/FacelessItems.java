package bw.development.facelessItems;

import bw.development.facelessItems.Commands.FacelessItemsCommand;
import bw.development.facelessItems.Commands.GiveItemCommand;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Listeners.ItemEventListener;
import bw.development.facelessItems.Rarity.RarityManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FacelessItems extends JavaPlugin {

    private CustomItemManager customItemManager;
    private RarityManager rarityManager;

    @Override
    public void onEnable() {
        // Inicializar gestores
        this.rarityManager = new RarityManager(this);
        this.rarityManager.loadRarities();

        this.customItemManager = new CustomItemManager(this);
        this.customItemManager.loadItems();

        // Registrar listeners y comandos
        getServer().getPluginManager().registerEvents(new ItemEventListener(this), this);

        this.getCommand("giveitem").setExecutor(new GiveItemCommand(this));
        this.getCommand("facelessitems").setExecutor(new FacelessItemsCommand(this));

        getLogger().info("FacelessItems habilitado con " + customItemManager.getItemCount() + " items cargados.");
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
