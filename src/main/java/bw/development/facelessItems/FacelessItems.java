package bw.development.facelessItems;

import bw.development.facelessItems.Api.FacelessItemsAPI;
import bw.development.facelessItems.Commands.FacelessItemsCommand;
import bw.development.facelessItems.Commands.GiveItemCommand;
import bw.development.facelessItems.Commands.GiveItemTabCompleter;
import bw.development.facelessItems.Effects.CooldownManager;
import bw.development.facelessItems.Effects.ShadowCloneEffect;
import bw.development.facelessItems.Gui.GuiListener;
import bw.development.facelessItems.Gui.ItemsGUI;
import bw.development.facelessItems.Items.CustomItemManager;
import bw.development.facelessItems.Listeners.ItemEventListener;
import bw.development.facelessItems.Managers.MessageManager;
import bw.development.facelessItems.Rarity.RarityManager;
import bw.development.facelessItems.Sets.SetEquipmentChecker;
import bw.development.facelessItems.Sets.SetManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FacelessItems extends JavaPlugin {
    private static FacelessItems instance;
    private CustomItemManager customItemManager;
    private RarityManager rarityManager;
    private CooldownManager cooldownManager;
    private MessageManager messageManager;
    private SetManager setManager;
    private SetEquipmentChecker setEquipmentChecker; // <- CAMPO RENOMBRADO
    private ItemsGUI itemsGUI;
    private ItemEventListener itemEventListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig(); // Good practice to ensure config.yml exists

        try {
            this.messageManager = new MessageManager(this);
            this.cooldownManager = new CooldownManager(); // 3. Initialize the manager
            this.rarityManager = new RarityManager(this);
            this.setManager = new SetManager(this);

            this.rarityManager.loadRarities();

            this.customItemManager = new CustomItemManager(this);
            this.customItemManager.loadItems();

            // Inicializamos la API y le pasamos el gestor de ítems.
            FacelessItemsAPI.initialize(customItemManager);

            this.itemsGUI = new ItemsGUI(customItemManager);

            // Usamos la nueva clase refactorizada para revisar equipos y sets.
            this.setEquipmentChecker = new SetEquipmentChecker(this, setManager);
            this.setEquipmentChecker.runTaskTimer(this, 0L, 20L); // Scheduler activo

            getServer().getPluginManager().registerEvents(new GuiListener(itemsGUI), this);
            this.itemEventListener = new ItemEventListener(this, customItemManager);
            getServer().getPluginManager().registerEvents(this.itemEventListener, this);

            if (getCommand("giveitem") == null) {
                getLogger().severe("El comando 'giveitem' no está registrado en plugin.yml!");
            } else {
                // --- LÍNEA MODIFICADA ---
                getCommand("giveitem").setExecutor(new GiveItemCommand(customItemManager, messageManager));
                // El TabCompleter no necesita el MessageManager
                getCommand("giveitem").setTabCompleter(new GiveItemTabCompleter(customItemManager));
            }

            if (getCommand("facelessitems") == null) {
                getLogger().severe("El comando 'facelessitems' no está registrado en plugin.yml!");
            } else {
                // --- LÍNEA MODIFICADA ---
                getCommand("facelessitems").setExecutor(new FacelessItemsCommand(this, messageManager));
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
    public SetEquipmentChecker getSetEquipmentChecker() {
        return setEquipmentChecker;
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

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public SetManager getSetManager() {
        return setManager;
    }

    public ItemsGUI getItemsGUI() {
        return itemsGUI;
    }

    public ItemEventListener getItemEventListener() {
        return this.itemEventListener;
    }
}