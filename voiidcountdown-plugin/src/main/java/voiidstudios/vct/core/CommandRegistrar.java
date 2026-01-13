package voiidstudios.vct.core;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.commands.MainCommand;

public class CommandRegistrar {
    private final VoiidCountdownTimer plugin;

    public CommandRegistrar(VoiidCountdownTimer plugin) {
        this.plugin = plugin;
    }

    public void register() {
        plugin.getCommand("voiidcountdowntimer").setExecutor(new MainCommand());
    }
}
