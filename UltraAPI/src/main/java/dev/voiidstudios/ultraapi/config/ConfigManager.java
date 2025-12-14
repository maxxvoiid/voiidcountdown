package dev.voiidstudios.ultraapi.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.plugin.Plugin;

/**
 * Manages {@link UConfig} instances to avoid reloading the same file repeatedly.
 */
public class ConfigManager {

    private final Map<String, UConfig> configs = new HashMap<>();

    public ConfigManager() {
    }

    /**
     * Retrieves (or creates) a configuration file stored inside the plugin's data folder.
     *
     * @param plugin   owning plugin
     * @param fileName relative file path inside the data folder
     * @return shared {@link UConfig}
     */
    public UConfig config(Plugin plugin, String fileName) {
        return config(plugin, plugin.getDataFolder(), fileName);
    }

    /**
     * Retrieves (or creates) a configuration file stored inside a specific folder.
     *
     * @param plugin      owning plugin
     * @param parent      directory where the file should live
     * @param fileName    file name (subfolders supported)
     * @return shared {@link UConfig}
     */
    public UConfig config(Plugin plugin, File parent, String fileName) {
        String key = buildKey(plugin, parent, fileName);
        return configs.computeIfAbsent(key, k -> new UConfig(plugin, parent, fileName, plugin.getLogger()));
    }

    private String buildKey(Plugin plugin, File parent, String fileName) {
        return plugin.getName() + "::" + parent.getAbsolutePath() + "::" + fileName;
    }
}
