package dev.voiidstudios.ultraapi;

import dev.voiidstudios.ultraapi.config.ConfigManager;
import dev.voiidstudios.ultraapi.config.UConfig;
import dev.voiidstudios.ultraapi.updates.UpdateService;
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
    private static UConfig sharedConfig;
    private static UpdateService updateService;

    @Override
    public void onEnable() {
        instance = this;
        configManager = new ConfigManager(getLogger());
        sharedConfig = config("config.yml");
        updateService = new UpdateService(getLogger(), sharedConfig);
        getLogger().info("UltraAPI enabled. Configuration and update utilities are ready to use.");
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
     * Provides access to a configuration stored inside UltraAPI's data folder.
     * Defaults are read from UltraAPI's resources, making the file reusable across plugins.
     *
     * @param fileName relative file path under the UltraAPI data folder
     * @return managed {@link UConfig}
     */
    public static UConfig config(String fileName) {
        return getConfigManager().config(getInstance(), getInstance().getDataFolder(), fileName);
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

    /**
     * @return update service used by plugins to perform centralized checks
     */
    public static UpdateService updates() {
        if (updateService == null) {
            updateService = new UpdateService(instance.getLogger(), sharedConfig != null ? sharedConfig : config("config.yml"));
        }
        return updateService;
    }

    /**
     * @return shared configuration stored in the UltraAPI data folder
     */
    public static UConfig sharedConfig() {
        if (sharedConfig == null) {
            sharedConfig = config("config.yml");
        }
        return sharedConfig;
    }
}
