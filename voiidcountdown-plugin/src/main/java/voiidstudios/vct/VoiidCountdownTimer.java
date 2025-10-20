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
import voiidstudios.vct.listeners.PlayerListener;
import voiidstudios.vct.managers.DependencyManager;
import voiidstudios.vct.managers.DynamicsManager;
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TimerStateManager;
import voiidstudios.vct.utils.ServerCompatibility;
import voiidstudios.vct.utils.ServerVersion;

public final class VoiidCountdownTimer extends JavaPlugin {
    public static String prefix = "&5[&dVCT&5] ";
    public String version = getDescription().getVersion();

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

    public void onEnable() {
        instance = this;
        configsManager = new ConfigsManager(this);
        messagesManager = new MessagesManager(this);
        configsManager.configure();
        setVersion();
        registerCommands();
        registerEvents();

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
        }

        MessagesManager.setPrefix(prefix);

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&6        __ ___"));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&5  \\  / &6|    |    &dVoiid &eCountdown Timer"));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&5   \\/  &6|__  |    &8Running v" + version + " on " + serverName + " (" + cleanVersion + ")"));
        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(""));

        new Metrics(this, 26790);
        dependencyManager = new DependencyManager(this);
        dynamicsManager = new DynamicsManager(this);
        updateChecker = new UpdateChecker(version);

        checkUpdates(updateChecker.check());

        timerStateManager = new TimerStateManager(this);
        timerStateManager.loadState();
    }

    public void onDisable() {
        if (timerStateManager != null && configsManager.getMainConfigManager().isSave_state_timers()) {
            timerStateManager.saveState();
        }

        Bukkit.getConsoleSender().sendMessage(
                MessagesManager.getColoredMessage(prefix+"&aHas been disabled! Goodbye ;)")
        );
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
            default:
                try{
                    serverVersion = ServerVersion.valueOf(packageName.replace("org.bukkit.craftbukkit.", ""));
                }catch(Exception e){
                    serverVersion = ServerVersion.v1_21_R5;
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
                    Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&bAn stable update for Voiid Countdown Timer &e("+latestVersion+") &bis available. Downloading shortly..."));

                    if (ServerCompatibility.isFolia()) {
                        Bukkit.getGlobalRegionScheduler().runDelayed(this, scheduledTask -> UpdateDownloaderGithub.downloadUpdate(), 2L);
                    } else {
                        Bukkit.getScheduler().runTaskAsynchronously(this, () -> UpdateDownloaderGithub.downloadUpdate());
                    }
                }
            }
        }else{
            if (configsManager.getMainConfigManager().isUpdate_notification() && !configsManager.getMainConfigManager().isAuto_update()) Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(prefix+"&cAn error occurred while checking for updates."));
        }
    }

    public void sendConsoleUpdateMessage(String latestVersion){
        if(latestVersion != null){
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&bAn stable update for Voiid Countdown Timer &e("+latestVersion+") &bis available."));
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&bYou can download it at: &fhttps://modrinth.com/datapack/voiid-countdown-timer"));
        }
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
}