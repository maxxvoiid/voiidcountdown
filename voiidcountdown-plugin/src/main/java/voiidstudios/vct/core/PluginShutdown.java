package voiidstudios.vct.core;

import voiidstudios.vct.VoiidCountdownTimer;

public class PluginShutdown {
    private final VoiidCountdownTimer plugin;

    public PluginShutdown(VoiidCountdownTimer plugin) {
        this.plugin = plugin;
    }

    public void disable() {
        plugin.shutdownTimerState();
        plugin.shutdownExpansions();
        plugin.getMessagesManager().console(VoiidCountdownTimer.prefix + "&aHas been disabled! Goodbye ;)");
    }
}
