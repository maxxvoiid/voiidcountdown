package voiidstudios.vct;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import voiidstudios.vct.api.Metrics;
import voiidstudios.vct.api.PAPIExpansion;
import voiidstudios.vct.api.update.UpdateChecker;
import voiidstudios.vct.api.update.UpdateCheckerResult;
import voiidstudios.vct.api.update.UpdateDownloaderGithub;
import voiidstudios.vct.commands.MainCommand;
import voiidstudios.vct.configs.ConfigsManager;
import voiidstudios.vct.configs.MainConfigManager;
import voiidstudios.vct.expansions.ExpansionManager;
import voiidstudios.vct.listeners.PlayerListener;
import voiidstudios.vct.managers.DependencyManager;
import voiidstudios.vct.managers.DynamicsManager;
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TimerStateManager;
import voiidstudios.vct.utils.ServerCompatibility;
import voiidstudios.vct.utils.ServerVersion;

public final class VoiidCountdownTimer extends JavaPlugin {
    public static String prefix = "&5[&dVCT&5] ";
    public static String prefixLogger = "§5[§dVCT§5] ";
    public String version = getDescription().getVersion();

    private static final String VCT_LOADED_PROPERTY = "vct.jvm.loaded";

    private final String serverName = Bukkit.getServer().getName();
    private final String bukkitVersion = Bukkit.getBukkitVersion();
    private final String cleanVersion = bukkitVersion.split("-")[0];

    public static ServerVersion serverVersion;
    private static VoiidCountdownTimer instance;
    private UpdateChecker updateChecker;
    private static ConfigsManager configsManager;
    private static DynamicsManager dynamicsManager;
    private static MessagesManager messagesManager;
    private static TimerStateManager timerStateManager;
    private static DependencyManager dependencyManager;
    private static ExpansionManager expansionManager;

    public void onEnable() {
        instance = this;

        if (Boolean.getBoolean(VCT_LOADED_PROPERTY)) {
            sendConsoleUnstableReloadMessage();
        } else {
            System.setProperty(VCT_LOADED_PROPERTY, "true");
        }

        configsManager = new ConfigsManager(this);
        configsManager.configure();

        if (configsManager.getMainConfigManager().getConfig().contains("Messages")) {
            sendConsoleLegacyMessagesConfigMessage();
        }

        messagesManager = new MessagesManager(this);
        MessagesManager.setPrefix(prefix);
        messagesManager.loadLanguage(
                configsManager.getMainConfigManager().getLanguage()
        );

        messagesManager.debug("&bDebug mode enabled, you will see many debug messages only on your server console! ;)");
        
        messagesManager.debug("&6Initializing commands and events");

        setVersion();
        registerCommands();
        registerEvents();

        messagesManager.debug("&6Setting up placeholders on PlaceholderAPI");

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
        }

        messagesManager.console("&6        __ ___");
        messagesManager.console("&5  \\  / &6|    |    &dVoiid &eCountdown Timer");
        messagesManager.console("&5   \\/  &6|__  |    &8Running v" + version + " on " + serverName + " (" + cleanVersion + ")");
        messagesManager.console("");

        messagesManager.debug("&6Setting up bStats metrics");

        new Metrics(this, 26790);

        dependencyManager = new DependencyManager(this);
        dynamicsManager = new DynamicsManager(this);
        updateChecker = new UpdateChecker(version);

        messagesManager.debug("&6Checking for updates");

        checkUpdates(updateChecker.check());

        messagesManager.debug("&6Checking if there is a timer state");

        timerStateManager = new TimerStateManager(this);
        timerStateManager.loadState();

        messagesManager.debug("&6Loading expansions");

        expansionManager = new ExpansionManager(this);
        expansionManager.loadExpansions();
    }

    public void onDisable() {
        messagesManager.debug("&6Checking for an active timer");
        
        if (timerStateManager != null && configsManager.getMainConfigManager().isSave_state_timers()) {
            timerStateManager.saveState();
        }

        messagesManager.debug("&6Disabling expansions");

        if (expansionManager != null) {
            expansionManager.shutdown();
        }

        messagesManager.console(prefix+"&aHas been disabled! Goodbye ;)");
    }

    public void setVersion(){
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        switch(bukkitVersion){
            case "1.20.5":
            case "1.20.6":
                serverVersion = ServerVersion.v1_20_R4;
                break;
            case "1.21":
            case "1.21.1":
                serverVersion = ServerVersion.v1_21_R1;
                break;
            case "1.21.2":
            case "1.21.3":
                serverVersion = ServerVersion.v1_21_R2;
                break;
            case "1.21.4":
                serverVersion = ServerVersion.v1_21_R3;
                break;
            case "1.21.5":
                serverVersion = ServerVersion.v1_21_R4;
                break;
            case "1.21.6":
            case "1.21.7":
            case "1.21.8":
                serverVersion = ServerVersion.v1_21_R5;
                break;
			case "1.21.9":
			case "1.21.10":
				serverVersion = ServerVersion.v1_21_R6;
				break;
            default:
                try{
                    serverVersion = ServerVersion.valueOf(packageName.replace("org.bukkit.craftbukkit.", ""));
                }catch(Exception e){
                    serverVersion = ServerVersion.v1_21_R6;
                }
        }
    }

    public void registerCommands() {
        this.getCommand("voiidcountdowntimer").setExecutor(new MainCommand());
    }

    public void registerEvents() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public static VoiidCountdownTimer getInstance() {
        return instance;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public void checkUpdates(UpdateCheckerResult result){
        if(!result.isError()){
            String latestVersion = result.getLatestVersion();

            if (configsManager.getMainConfigManager().isUpdate_notification() && !configsManager.getMainConfigManager().isAuto_update()) sendConsoleUpdateMessage(latestVersion);

            if (configsManager.getMainConfigManager().isAuto_update()) {
                if (latestVersion != null && !latestVersion.equalsIgnoreCase(version)) {
                    messagesManager.console("&bAn stable update for Voiid Countdown Timer &e("+latestVersion+") &bis available. Downloading shortly...");

                    if (ServerCompatibility.isFolia()) {
                        Bukkit.getGlobalRegionScheduler().runDelayed(this, scheduledTask -> UpdateDownloaderGithub.downloadUpdate(), 2L);
                    } else {
                        Bukkit.getScheduler().runTaskAsynchronously(this, () -> UpdateDownloaderGithub.downloadUpdate());
                    }
                }
            }
        }else{
            if (configsManager.getMainConfigManager().isUpdate_notification() && !configsManager.getMainConfigManager().isAuto_update()) messagesManager.console(prefix+"&cAn error occurred while checking for updates.");
        }
    }

    public void sendConsoleUpdateMessage(String latestVersion){
        if(latestVersion != null){
            messagesManager.console("&bAn stable update for Voiid Countdown Timer &e("+latestVersion+") &bis available.");
            messagesManager.console("&bYou can download it at: &fhttps://modrinth.com/datapack/voiid-countdown-timer");
        }
    }

    public void sendConsoleUnstableReloadMessage(){
        getLogger().severe("Server reload detected. This action is NOT supported and may break VCT and ALL dependent plugins! Please restart your server properly.");
    }

    public void sendConsoleLegacyMessagesConfigMessage(){
        getLogger().warning("=====================================");
        getLogger().warning(" Voiid Countdown Timer - Nobelium 2.1.0 Update");
        getLogger().warning(" Legacy 'Messages:' section detected in config.yml");
        getLogger().warning(" This section is no longer used.");
        getLogger().warning(" All messages are now handled through:");
        getLogger().warning("   /core/messages/origins/");
        getLogger().warning("   /core/messages/custom/");
        getLogger().warning("");
        getLogger().warning(" Delete the entire 'Messages' section in config.yml");
        getLogger().warning(" so that this message no longer appears.");
        getLogger().warning("=====================================");
    }

    public static ConfigsManager getConfigsManager() {
        return configsManager;
    }

    public static DynamicsManager getPhasesManager() {
        return dynamicsManager;
    }

    public static DependencyManager getDependencyManager() {
        return dependencyManager;
    }

    public static MessagesManager getMessagesManager() {
		return messagesManager;
	}

    public static TimerStateManager getTimerStateManager() {
        return timerStateManager;
    }

    public static ExpansionManager getExpansionManager() {
        return expansionManager;
    }
}