package dev.voiidstudios.ultraapi.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

/**
 * Thin wrapper around {@link YamlConfiguration} offering safe accessors and automatic creation with defaults.
 */
public class UConfig {

    private final Plugin plugin;
    private final File parentFolder;
    private final String fileName;
    private final Logger logger;

    private File file;
    private FileConfiguration configuration;

    public UConfig(Plugin plugin, File parentFolder, String fileName, Logger logger) {
        this.plugin = plugin;
        this.parentFolder = parentFolder;
        this.fileName = fileName;
        this.logger = logger;
        reload();
    }

    public UConfig(Plugin plugin, String fileName) {
        this(plugin, plugin.getDataFolder(), fileName, plugin.getLogger());
    }

    /**
     * Reloads the configuration from disk and reapplies defaults (if present in the plugin jar).
     */
    public final void reload() {
        prepareFile();

        configuration = new YamlConfiguration();
        try {
            configuration.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            log(Level.SEVERE, "Could not load configuration " + file.getName(), e);
        }

        applyDefaults();
    }

    /**
     * Saves the configuration to disk if it has been loaded.
     */
    public void save() {
        if (configuration == null) {
            reload();
        }

        try {
            configuration.save(file);
        } catch (IOException e) {
            log(Level.SEVERE, "Could not save configuration " + file.getName(), e);
        }
    }

    /**
     * @return the underlying {@link FileConfiguration}
     */
    public FileConfiguration getConfiguration() {
        if (configuration == null) {
            reload();
        }
        return configuration;
    }

    /**
     * @return the backing file
     */
    public File getFile() {
        return file;
    }

    public String getString(String path, String defaultValue) {
        FileConfiguration cfg = getConfiguration();
        if (!cfg.contains(path)) {
            return defaultValue;
        }
        String value = cfg.getString(path);
        return value != null ? value : defaultValue;
    }

    public int getInt(String path, int defaultValue) {
        FileConfiguration cfg = getConfiguration();
        if (!cfg.contains(path)) {
            return defaultValue;
        }
        return cfg.getInt(path, defaultValue);
    }

    public boolean getBoolean(String path, boolean defaultValue) {
        FileConfiguration cfg = getConfiguration();
        if (!cfg.contains(path)) {
            return defaultValue;
        }
        return cfg.getBoolean(path, defaultValue);
    }

    private void prepareFile() {
        if (!parentFolder.exists() && !parentFolder.mkdirs()) {
            log(Level.WARNING, "Could not create parent folder for " + fileName, null);
        }

        file = new File(parentFolder, fileName);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        if (!file.exists()) {
            if (!copyDefaultResource()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    log(Level.SEVERE, "Could not create configuration " + file.getName(), e);
                }
            }
        }
    }

    private void applyDefaults() {
        InputStream defStream = plugin.getResource(fileName);
        if (defStream == null) {
            return;
        }

        YamlConfiguration defaults = YamlConfiguration.loadConfiguration(new InputStreamReader(defStream, StandardCharsets.UTF_8));
        configuration.setDefaults(defaults);
        configuration.options().copyDefaults(true);
    }

    private boolean copyDefaultResource() {
        InputStream resource = plugin.getResource(fileName);
        if (resource == null) {
            return false;
        }

        try {
            resource.close();
        } catch (IOException ignored) {
        }

        try {
            plugin.saveResource(fileName, false);
            return true;
        } catch (IllegalArgumentException ex) {
            // Resource path might not exist inside the jar; fall through to file creation.
            return false;
        }
    }

    private void log(Level level, String message, Exception e) {
        if (logger != null) {
            if (e != null) {
                logger.log(level, message, e);
            } else {
                logger.log(level, message);
            }
        }
    }
}
