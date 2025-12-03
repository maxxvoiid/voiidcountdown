package voiidstudios.vct.expansions;


import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.expansions.command.ExpansionCommandRegistry;
import voiidstudios.vct.expansions.exceptions.InvalidExpansionException;
import voiidstudios.vct.managers.MessagesManager;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class ExpansionManager {
    private final VoiidCountdownTimer plugin;
    private final ExpansionCommandRegistry commandRegistry = new ExpansionCommandRegistry();
    private final Map<String, ScriptExpansion> loadedExpansions = new LinkedHashMap<>();
    private final Map<String, ExpansionHandle> discoveredExpansions = new LinkedHashMap<>();
    private final MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();
    private final File expansionsDirectory;

    private static final String METADATA_FILE = "expansion.yml";

    public ExpansionManager(VoiidCountdownTimer plugin) {
        this.plugin = plugin;
        this.expansionsDirectory = new File(plugin.getDataFolder(), "expansions");
    }

    public void loadExpansions() {
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Unable to create the plugin data folder. Expansions will be disabled.");
            return;
        }

        if (!expansionsDirectory.exists() && !expansionsDirectory.mkdirs()) {
            plugin.getLogger().warning("Unable to create the expansions directory. Expansions will be disabled.");
            return;
        }

        refreshDiscoveredExpansions();

        if (discoveredExpansions.isEmpty()) {
            msgManager.debug("No expansions were found to load.");
            return;
        }

        int loadedCount = 0;
        int skippedCount = 0;

        for (ExpansionHandle handle : discoveredExpansions.values()) {
            if (!handle.metadata.isEnabled()) {
                skippedCount++;
                msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&3The &e%s&3 extension has been skipped because it is disabled.", handle.metadata.getName()));
                continue;
            }

            if (loadExpansion(handle)) {
                loadedCount++;
            } else {
                skippedCount++;
            }
        }

        msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&a%d expansion(s) loaded! &3%d expansion(s) skipped.", loadedCount, skippedCount));
    }

    private boolean loadExpansion(ExpansionHandle handle) {
        if (handle == null) {
            return false;
        }

        if (loadedExpansions.containsKey(handle.key)) {
            return true;
        }

        if (!handle.metadata.isEnabled()) {
            return false;
        }

        try {
            ScriptExpansion expansion = new ScriptExpansion(plugin, handle.metadata, handle.directory, commandRegistry);
            if (expansion.load()) {
                loadedExpansions.put(handle.key, expansion);
                msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&aLoaded expansion %s &ev%s", handle.metadata.getName(), handle.metadata.getVersion()));
                return true;
            }
        } catch (InvalidExpansionException exception) {
            plugin.getLogger().log(
                Level.WARNING,
                String.format(Locale.ROOT, "Failed to initialize expansion %s", handle.metadata.getName()),
                exception
            );

            msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&cUnable to load expansion %s", handle.metadata.getName()));
        }

        return false;
    }

    public void shutdown() {
        for (ScriptExpansion expansion : new ArrayList<>(loadedExpansions.values())) {
            try {
                expansion.disable();
            } catch (Exception exception) {
                plugin.getLogger().log(
                    Level.WARNING,
                    String.format(Locale.ROOT, "Error while disabling expansion %s", expansion.getMetadata().getName()),
                    exception
                );

                msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&cUnable to disable expansion %s", expansion.getMetadata().getName()));
            }
        }

        loadedExpansions.clear();
        discoveredExpansions.clear();
    }

    public ExpansionCommandRegistry getCommandRegistry() {
        return commandRegistry;
    }

    public List<String> getHelpLines() {
        return commandRegistry.getHelpLines();
    }

    public List<String> getRootSuggestions(String partial) {
        return commandRegistry.getRootSuggestions(partial);
    }

    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> result = commandRegistry.tabComplete(sender, args);
        return result == null ? null : new ArrayList<>(result);
    }

    public boolean executeCommand(CommandSender sender, String[] args) {
        return commandRegistry.execute(sender, args);
    }

    public Map<String, ScriptExpansion> getLoadedExpansions() {
        return Collections.unmodifiableMap(loadedExpansions);
    }

    public boolean enableExpansion(String name) {
        refreshDiscoveredExpansions();
        ExpansionHandle handle = discoveredExpansions.get(normalizeName(name));
        if (handle == null) {
            return false;
        }

        if (loadedExpansions.containsKey(handle.key)) {
            return false;
        }

        String key = handle.key;
        boolean wasEnabled = handle.metadata.isEnabled();

        boolean updated = wasEnabled || updateEnabledFlag(handle.directory, true);
        if (!updated) {
            return false;
        }

        refreshDiscoveredExpansions();
        handle = discoveredExpansions.get(key);
        if (handle == null || !handle.metadata.isEnabled()) {
            return false;
        }

        if (!loadExpansion(handle)) {
            if (!wasEnabled) {
                updateEnabledFlag(handle.directory, false);
            }
            return false;
        }

        return true;
    }

    public boolean disableExpansion(String name) {
        String key = normalizeName(name);
        ScriptExpansion expansion = loadedExpansions.get(key);
        if (expansion == null) {
            return false;
        }

        boolean success = true;
        try {
            expansion.disable();
        } catch (Exception exception) {
            plugin.getLogger().log(
                Level.WARNING,
                String.format(Locale.ROOT, "Error while disabling expansion %s", expansion.getMetadata().getName()),
                exception
            );

            msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&cUnable to disable expansion %s", expansion.getMetadata().getName()));

            success = false;
        }

        if (success) {
            loadedExpansions.remove(key);
            success = updateEnabledFlag(expansion.getDirectory(), false);
        }

        return success;
    }

    public boolean reloadExpansion(String name) {
        String key = normalizeName(name);
        ScriptExpansion previous = loadedExpansions.get(key);

        if (previous != null) {
            try {
                previous.disable();
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to disable expansion " + key + " during reload.");
                return false;
            }
        }

        refreshDiscoveredExpansions();
        ExpansionHandle handle = discoveredExpansions.get(key);

        if (handle == null) {
            return false;
        }

        if (loadExpansion(handle)) {
            return true;
        } else {
            if (previous != null) {
                try {
                    previous.load();
                    loadedExpansions.put(key, previous);
                    plugin.getLogger().warning("Reload of expansion " + key + " failed. Restored previous version.");
                } catch (Exception ex) {
                    plugin.getLogger().warning("Failed to restore previous version of expansion " + key);
                }
            }
            return false;
        }
    }

    public int reloadAllExpansions() {
        List<ScriptExpansion> previous = new ArrayList<>(loadedExpansions.values());
        for (ScriptExpansion exp : previous) {
            try { exp.disable(); } catch (Exception ignored) {}
        }
        loadedExpansions.clear();

        refreshDiscoveredExpansions();

        int loadedCount = 0;
        for (ExpansionHandle handle : discoveredExpansions.values()) {
            if (handle.metadata.isEnabled() && loadExpansion(handle)) {
                loadedCount++;
            }
        }

        return loadedCount;
    }

    public ExpansionMetadata getExpansionMetadata(String name) {
        String key = normalizeName(name);
        refreshDiscoveredExpansions();

        ExpansionHandle handle = discoveredExpansions.get(key);
        if (handle != null) {
            return handle.metadata;
        }

        ScriptExpansion expansion = loadedExpansions.get(key);
        return expansion == null ? null : expansion.getMetadata();
    }

    public boolean isExpansionLoaded(String name) {
        return loadedExpansions.containsKey(normalizeName(name));
    }

    public List<String> getKnownExpansionNames() {
        refreshDiscoveredExpansions();

        java.util.LinkedHashSet<String> names = new java.util.LinkedHashSet<>();
        for (ExpansionHandle handle : discoveredExpansions.values()) {
            names.add(handle.metadata.getName());
        }

        for (ScriptExpansion expansion : loadedExpansions.values()) {
            names.add(expansion.getMetadata().getName());
        }

        return new ArrayList<>(names);
    }

    private void refreshDiscoveredExpansions() {
        discoveredExpansions.clear();

        File[] children = expansionsDirectory.listFiles(File::isDirectory);
        if (children == null) {
            return;
        }

        for (File child : children) {
            File metadataFile = new File(child, METADATA_FILE);
            if (!metadataFile.exists()) {
                msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&3Skipping expansion in %s because %s is missing", child.getName(), METADATA_FILE));

                continue;
            }

            ExpansionMetadata metadata;
            try {
                metadata = ExpansionMetadata.fromFile(metadataFile);
            } catch (InvalidExpansionException exception) {
                plugin.getLogger().log(
                    Level.WARNING,
                    String.format(Locale.ROOT, "Invalid expansion metadata in %s", child.getName()),
                    exception
                );

                msgManager.console(VoiidCountdownTimer.prefix + String.format(Locale.ROOT, "&cInvalid expansion metadata in %s", child.getName(), METADATA_FILE));

                continue;
            }

            String key = metadata.getName().toLowerCase(Locale.ROOT);
            if (discoveredExpansions.containsKey(key)) {
                plugin.getLogger().warning(String.format(Locale.ROOT, "Multiple expansions share the name %s. Skipping directory %s.", metadata.getName(), child.getName()));
                continue;
            }

            discoveredExpansions.put(key, new ExpansionHandle(key, child, metadata));
        }
    }

    private boolean updateEnabledFlag(File directory, boolean enabled) {
        if (directory == null) {
            return false;
        }

        File metadataFile = new File(directory, METADATA_FILE);
        if (!metadataFile.exists()) {
            return false;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(metadataFile);
        configuration.set("enabled", enabled);

        try {
            configuration.save(metadataFile);
            return true;
        } catch (IOException exception) {
            plugin.getLogger().log(
                Level.WARNING,
                String.format(Locale.ROOT, "Unable to update enabled state for expansion in %s", directory.getName()),
                exception
            );
            return false;
        }
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.toLowerCase(Locale.ROOT);
    }

    private static final class ExpansionHandle {
        private final String key;
        private final File directory;
        private final ExpansionMetadata metadata;

        private ExpansionHandle(String key, File directory, ExpansionMetadata metadata) {
            this.key = key;
            this.directory = directory;
            this.metadata = metadata;
        }
    }
}