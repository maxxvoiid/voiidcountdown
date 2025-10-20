package voiidstudios.vct.addons.stopwatch;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import voiidstudios.vct.api.VCTAPI;

public final class StopwatchAddonPlugin extends JavaPlugin {

    public static final String ADDON_ID = "stopwatch";
    private StopwatchAddon addon;

    @Override
    public void onEnable() {
        if (!VCTAPI.isAvailable()) {
            getLogger().severe("VoiidCountdownTimer is not available. Disabling stopwatch addon.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        addon = new StopwatchAddon(this);
        boolean registered = VCTAPI.registerAddon(ADDON_ID, addon, this);
        if (!registered) {
            getLogger().severe("Unable to register stopwatch addon with VoiidCountdownTimer. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        StopwatchCommand command = new StopwatchCommand(addon);
        PluginCommand pluginCommand = getCommand("vctstopwatch");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
            pluginCommand.setTabCompleter(command);
        }
    }

    @Override
    public void onDisable() {
        if (addon != null) {
            VCTAPI.unregisterAddon(ADDON_ID);
            addon = null;
        }
    }
}
