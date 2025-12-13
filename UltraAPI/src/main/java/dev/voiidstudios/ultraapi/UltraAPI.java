package dev.voiidstudios.ultraapi;

import dev.voiidstudios.ultraapi.config.ConfigManager;
import dev.voiidstudios.ultraapi.config.UConfig;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * UltraAPI bootstraps shared utilities that can be consumed by any plugin.
 * <p>
 * The configuration layer exposes a thin wrapper around {@link org.bukkit.configuration.file.YamlConfiguration}
 * to simplify safe reads and default handling.
 */
public final class UltraAPI extends JavaPlugin {

    private static UltraAPI instance;
    private static ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(getLogger());
        getLogger().info("UltraAPI enabled. Configuration utilities are ready to use.");
    }

    @Override
    public void onDisable() {
        getLogger().info("UltraAPI disabled.");
    }

    /**
     * Provides access to a managed configuration file stored in the owning plugin's data folder.
     *
     * @param plugin   the plugin requesting the configuration
     * @param fileName the relative path of the configuration file (supports subfolders)
     * @return a managed {@link UConfig} instance
     */
    public static UConfig config(Plugin plugin, String fileName) {
        return getConfigManager().config(plugin, fileName);
    }

    /**
     * Exposes the shared {@link ConfigManager} instance.
     *
     * @return the singleton config manager
     */
    public static ConfigManager getConfigManager() {
        if (configManager == null) {
            configManager = new ConfigManager(instance != null ? instance.getLogger() : null);
        }
        return configManager;
    }

    /**
     * @return the running UltraAPI plugin instance
     */
    public static UltraAPI getInstance() {
        return instance;
    }
}
