package voiidstudios.vct.core;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.listeners.PlayerListener;

public class ListenerRegistrar {
    private final VoiidCountdownTimer plugin;

    public ListenerRegistrar(VoiidCountdownTimer plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(plugin), plugin);
    }
}
