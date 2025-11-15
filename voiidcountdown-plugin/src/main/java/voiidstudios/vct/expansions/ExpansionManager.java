package voiidstudios.vct.expansions;


import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.expansions.command.ExpansionCommandRegistry;
import voiidstudios.vct.expansions.exceptions.InvalidExpansionException;

import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

public class ExpansionManager {
    private final VoiidCountdownTimer plugin;
    private final ExpansionCommandRegistry commandRegistry = new ExpansionCommandRegistry();
    private final Map<String, ScriptExpansion> loadedExpansions = new LinkedHashMap<>();
    private final File expansionsDirectory;

    private static final String METADATA_FILE = "expansion.yml";
    private static final String[] BUNDLED_EXPANSIONS = new String[] {"stopwatch"};

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

        exportBundledExamples();

        File[] children = expansionsDirectory.listFiles(File::isDirectory);
        if (children == null || children.length == 0) {
            plugin.getLogger().info("No expansions were found to load.");
            return;
        }

        for (File child : children) {
            loadExpansion(child);
        }
    }

    private boolean loadExpansion(File directory) {
        ExpansionMetadata metadata = readMetadata(directory, true);
        if (metadata == null) {
            return false;
        }

        String key = metadata.getName().toLowerCase(Locale.ROOT);
        if (loadedExpansions.containsKey(key)) {
            plugin.getLogger().warning(String.format(Locale.ROOT, "An expansion with the name %s is already loaded. Skipping duplicate.", metadata.getName()));
            return false;
        }

        try {
            ScriptExpansion expansion = new ScriptExpansion(plugin, metadata, directory, commandRegistry);
            if (expansion.load()) {
                loadedExpansions.put(key, expansion);
                plugin.getLogger().info(String.format(Locale.ROOT, "Loaded expansion %s v%s", metadata.getName(), metadata.getVersion()));
                return true;
            }
        } catch (InvalidExpansionException exception) {
            plugin.getLogger().log(Level.WARNING, String.format(Locale.ROOT, "Failed to initialize expansion %s", metadata.getName()), exception);
        }

        return false;
    }

    private void exportBundledExamples() {
        for (String expansionId : BUNDLED_EXPANSIONS) {
            File targetDirectory = new File(expansionsDirectory, expansionId);
            if (!targetDirectory.exists() && !targetDirectory.mkdirs()) {
                plugin.getLogger().warning(String.format(Locale.ROOT, "Unable to create folder for example expansion %s", expansionId));
                continue;
            }

            copyResourceIfAbsent("expansions/" + expansionId + "/" + METADATA_FILE, new File(targetDirectory, METADATA_FILE));
            copyResourceIfAbsent("expansions/" + expansionId + "/main.js", new File(targetDirectory, "main.js"));
        }
    }

    private void copyResourceIfAbsent(String resourcePath, File destination) {
        if (destination.exists()) {
            return;
        }

        try (InputStream inputStream = plugin.getResource(resourcePath)) {
            if (inputStream == null) {
                return;
            }

            try (FileOutputStream outputStream = new FileOutputStream(destination)) {
                byte[] buffer = new byte[4096];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, len);
                }
            }
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, String.format(Locale.ROOT, "Unable to export resource %s", resourcePath), exception);
        }
    }

    public void shutdown() {
        for (ScriptExpansion expansion : new ArrayList<>(loadedExpansions.values())) {
            try {
                expansion.disable();
            } catch (Exception exception) {
                plugin.getLogger().log(Level.WARNING, String.format(Locale.ROOT, "Error while disabling expansion %s", expansion.getMetadata().getName()), exception);
            }
        }

        loadedExpansions.clear();
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

    public boolean isExpansionLoaded(String name) {
        return loadedExpansions.containsKey(name.toLowerCase(Locale.ROOT));
    }

    public ExpansionMetadata getExpansionMetadata(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        ScriptExpansion expansion = loadedExpansions.get(key);
        if (expansion != null) {
            return expansion.getMetadata();
        }

        File directory = findExpansionDirectory(name);
        if (directory == null) {
            return null;
        }

        return readMetadata(directory, false);
    }

    public ExpansionActionResult enableExpansion(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        if (loadedExpansions.containsKey(key)) {
            return ExpansionActionResult.ALREADY_ENABLED;
        }

        File directory = findExpansionDirectory(name);
        if (directory == null) {
            return ExpansionActionResult.NOT_FOUND;
        }

        return loadExpansion(directory) ? ExpansionActionResult.SUCCESS : ExpansionActionResult.FAILED;
    }

    public ExpansionActionResult disableExpansion(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        ScriptExpansion expansion = loadedExpansions.remove(key);
        if (expansion == null) {
            return ExpansionActionResult.ALREADY_DISABLED;
        }

        try {
            expansion.disable();
            return ExpansionActionResult.SUCCESS;
        } catch (Exception exception) {
            loadedExpansions.put(key, expansion);
            plugin.getLogger().log(Level.WARNING, String.format(Locale.ROOT, "Error while disabling expansion %s", expansion.getMetadata().getName()), exception);
            return ExpansionActionResult.FAILED;
        }
    }

    public ExpansionActionResult reloadExpansion(String name) {
        String key = name.toLowerCase(Locale.ROOT);
        ScriptExpansion existing = loadedExpansions.get(key);
        File directory = existing != null ? existing.getDirectory() : findExpansionDirectory(name);

        if (directory == null) {
            return ExpansionActionResult.NOT_FOUND;
        }

        if (existing != null) {
            ExpansionActionResult disableResult = disableExpansion(existing.getMetadata().getName());
            if (disableResult != ExpansionActionResult.SUCCESS && disableResult != ExpansionActionResult.ALREADY_DISABLED) {
                return disableResult;
            }
        }

        return loadExpansion(directory) ? ExpansionActionResult.SUCCESS : ExpansionActionResult.FAILED;
    }

    public int reloadAllExpansions() {
        shutdown();
        loadExpansions();
        return loadedExpansions.size();
    }

    public List<String> getKnownExpansionNames() {
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (ScriptExpansion expansion : loadedExpansions.values()) {
            names.add(expansion.getMetadata().getName());
        }

        File[] children = expansionsDirectory.listFiles(File::isDirectory);
        if (children != null) {
            for (File child : children) {
                ExpansionMetadata metadata = readMetadata(child, false);
                if (metadata != null) {
                    names.add(metadata.getName());
                }
            }
        }

        return new ArrayList<>(names);
    }

    public List<String> getLoadedExpansionNames() {
        List<String> names = new ArrayList<>();
        for (ScriptExpansion expansion : loadedExpansions.values()) {
            names.add(expansion.getMetadata().getName());
        }
        return names;
    }

    private ExpansionMetadata readMetadata(File directory, boolean logWarnings) {
        File metadataFile = new File(directory, METADATA_FILE);
        if (!metadataFile.exists()) {
            if (logWarnings) {
                plugin.getLogger().warning(String.format(Locale.ROOT, "Skipping expansion in %s because %s is missing", directory.getName(), METADATA_FILE));
            }
            return null;
        }

        try {
            return ExpansionMetadata.fromFile(metadataFile);
        } catch (InvalidExpansionException exception) {
            if (logWarnings) {
                plugin.getLogger().log(Level.WARNING, String.format(Locale.ROOT, "Invalid expansion metadata in %s", directory.getName()), exception);
            }
            return null;
        }
    }

    private File findExpansionDirectory(String name) {
        if (!expansionsDirectory.exists()) {
            return null;
        }

        String search = name.toLowerCase(Locale.ROOT);
        File[] children = expansionsDirectory.listFiles(File::isDirectory);
        if (children == null) {
            return null;
        }

        for (File child : children) {
            if (child.getName().equalsIgnoreCase(name)) {
                return child;
            }

            ExpansionMetadata metadata = readMetadata(child, false);
            if (metadata != null && metadata.getName().toLowerCase(Locale.ROOT).equals(search)) {
                return child;
            }
        }

        return null;
    }

    public enum ExpansionActionResult {
        SUCCESS,
        NOT_FOUND,
        ALREADY_ENABLED,
        ALREADY_DISABLED,
        FAILED
    }
}
