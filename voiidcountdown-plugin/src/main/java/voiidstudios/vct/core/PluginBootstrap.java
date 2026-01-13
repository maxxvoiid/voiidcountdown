package voiidstudios.vct.core;

import voiidstudios.vct.VoiidCountdownTimer;

public class PluginBootstrap {
    private final VoiidCountdownTimer plugin;

    public PluginBootstrap(VoiidCountdownTimer plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.checkReloadSafety();
        plugin.initializeConfigs();
        plugin.initializeMessages();
        plugin.getMessagesManager().debug("&6Initializing commands and events");
        plugin.setVersion();
        plugin.registerCommands();
        plugin.registerEvents();
        plugin.initializePlaceholders();
        plugin.logStartupBanner();
        plugin.initializeMetrics();
        plugin.initializeManagers();
        plugin.checkForUpdates();
        plugin.loadTimerState();
        plugin.loadExpansions();
    }
}
