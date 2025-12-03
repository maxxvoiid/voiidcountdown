package voiidstudios.vct.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import voiidstudios.vct.VoiidCountdownTimer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class TranslationManager {

    private final VoiidCountdownTimer plugin;

    private FileConfiguration jarLangBase;
    private FileConfiguration langBase;
    private FileConfiguration langSelected;
    private FileConfiguration langSelectedCustom;
    private FileConfiguration langCustomOverrides;

    private String currentLang = "en_US";

    public TranslationManager(VoiidCountdownTimer plugin) {
        this.plugin = plugin;
    }

    public void loadLanguage(String langCode) {
        this.currentLang = langCode;

        jarLangBase = loadYamlFromResource("core/messages/origins/en_US.yml");

        ensureDataFileExists("core/messages/origins/en_US.yml");

        langBase = loadYaml("core/messages/origins/en_US.yml");
        langSelected = loadYaml("core/messages/origins/" + langCode + ".yml");
        langSelectedCustom = loadYaml("core/messages/custom/" + langCode + ".yml");
        langCustomOverrides = loadYaml("core/messages/custom/custom.yml");

        syncMissingKeys();
    }

    private void ensureDataFileExists(String resourcePath) {
        File dest = new File(plugin.getDataFolder(), resourcePath);
        if (!dest.exists()) {
            InputStream is = plugin.getResource(resourcePath);
            if (is != null) {
                dest.getParentFile().mkdirs();
                try (InputStream in = is; FileOutputStream out = new FileOutputStream(dest)) {
                    byte[] buffer = new byte[4096];
                    int read;
                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                    }
                } catch (IOException e) {
                    plugin.getLogger().warning("Could not copy resource to data folder: " + resourcePath);
                }
            }
        }
    }

    private FileConfiguration loadYaml(String path) {
        File file = new File(plugin.getDataFolder(), path);
        if (!file.exists()) {
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(file);
    }

    private FileConfiguration loadYamlFromResource(String resourcePath) {
        InputStream is = plugin.getResource(resourcePath);
        if (is == null) return new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return YamlConfiguration.loadConfiguration(reader);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to load resource language file: " + resourcePath);
            return new YamlConfiguration();
        }
    }

    public void syncMissingKeys() {
        if (jarLangBase == null) return;
        if (langSelected == null) langSelected = new YamlConfiguration();
        if (langBase == null) langBase = new YamlConfiguration();

        File selectedFile = new File(plugin.getDataFolder(), "core/messages/origins/" + currentLang + ".yml");
        File baseFile = new File(plugin.getDataFolder(), "core/messages/origins/en_US.yml");

        boolean changedSelected = false;
        boolean changedBase = false;

        for (String key : jarLangBase.getKeys(true)) {
            Object value = jarLangBase.get(key);

            if (!langSelected.contains(key)) {
                langSelected.set(key, value);
                changedSelected = true;
                plugin.getLogger().info("Added missing translation key to " + currentLang + ": " + key);
            }

            if (!langBase.contains(key)) {
                langBase.set(key, value);
                changedBase = true;
                plugin.getLogger().info("Added missing base translation key: " + key);
            }
        }

        for (String key : langSelected.getKeys(true)) {
            if (!jarLangBase.contains(key) && !langBase.contains(key)) {
                Object value = langSelected.get(key);
                langBase.set(key, value);
                changedBase = true;
                plugin.getLogger().info("Propagated custom key from " + currentLang + " to base: " + key);
            }
        }

        try {
            if (changedSelected) {
                if (!selectedFile.getParentFile().exists()) selectedFile.getParentFile().mkdirs();
                langSelected.save(selectedFile);
            }
            if (changedBase) {
                if (!baseFile.getParentFile().exists()) baseFile.getParentFile().mkdirs();
                langBase.save(baseFile);
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save language files during sync.");
            e.printStackTrace();
        }
    }

    public String get(String key) {
        return getFromConfigurations(key, getBaseSources());
    }

    public List<String> getStringList(String key) {
        return asStringList(key, getBaseSources());
    }

    public String formatKey(String key, Map<String, String> placeholders) {
        String raw = get(key);
        if (raw == null) return null;
        return formatRaw(raw, placeholders);
    }

    public String formatRaw(String raw, Map<String, String> placeholders) {
        if (raw == null) return null;
        if (placeholders != null) {
            for (Map.Entry<String, String> e : placeholders.entrySet()) {
                raw = raw.replace(e.getKey(), e.getValue());
            }
        }
        return raw;
    }

    public void ensureExists(String key, String defaultValue) {
        boolean changed = false;

        if (langBase != null && !langBase.contains(key)) {
            langBase.set(key, defaultValue);
            changed = true;
        }

        if (langSelected != null && !langSelected.contains(key)) {
            langSelected.set(key, defaultValue);
            changed = true;
        }

        if (changed && langSelected != null) {
            try {
                langSelected.save(new File(plugin.getDataFolder(),
                        "core/messages/origins/" + currentLang + ".yml"));
            } catch (IOException ignored) {}
        }
    }

    public String getCurrentLanguage() {
        return currentLang;
    }

    private List<FileConfiguration> getBaseSources() {
        List<FileConfiguration> sources = new ArrayList<>();
        addIfNotEmpty(langCustomOverrides, sources);
        addIfNotEmpty(langSelectedCustom, sources);
        addIfNotEmpty(langSelected, sources);
        addIfNotEmpty(langBase, sources);
        return sources;
    }

    private void addIfNotEmpty(FileConfiguration config, List<FileConfiguration> list) {
        if (config == null || config.getKeys(true).isEmpty()) return;
        list.add(config);
    }

    private List<String> asStringList(String key, List<FileConfiguration> sources) {
        for (FileConfiguration source : sources) {
            if (source.isList(key)) {
                return source.getStringList(key);
            }
        }

        String raw = getFromConfigurations(key, sources);
        if (raw == null) {
            return Collections.emptyList();
        }

        if (!raw.contains("\n")) {
            return Collections.singletonList(raw);
        }

        return Arrays.asList(raw.split("\n"));
    }

    private String getFromConfigurations(String key, List<FileConfiguration> sources) {
        for (FileConfiguration config : sources) {
            if (config.contains(key)) {
                return config.getString(key);
            }
        }
        return null;
    }

    public TranslationBundle forCustomNamespace(String namespace) {
        return new TranslationBundle(namespace);
    }

    public class TranslationBundle {
        private final String namespace;
        private FileConfiguration namespacedSelected;
        private FileConfiguration namespacedDefault;
        private String lastLang;

        private TranslationBundle(String namespace) {
            this.namespace = namespace == null ? "" : namespace.trim();
            reload();
        }

        public void reload() {
            this.lastLang = currentLang;
            if (namespace.isEmpty()) {
                this.namespacedSelected = new YamlConfiguration();
                this.namespacedDefault = new YamlConfiguration();
                return;
            }

            this.namespacedSelected = loadYaml("core/messages/custom/" + namespace + "/" + currentLang + ".yml");
            this.namespacedDefault  = loadYaml("core/messages/custom/" + namespace + "/en_US.yml");
        }

        public String get(String key) {
            List<FileConfiguration> sources = getNamespacedSources();
            return getFromConfigurations(key, sources);
        }

        public List<String> getStringList(String key) {
            return asStringList(key, getNamespacedSources());
        }

        public String formatKey(String key, Map<String, String> placeholders) {
            return formatRaw(get(key), placeholders);
        }

        public String formatRaw(String raw, Map<String, String> placeholders) {
            if (raw == null) return null;
            if (placeholders != null) {
                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    raw = raw.replace(entry.getKey(), entry.getValue());
                }
            }
            return raw;
        }

        private List<FileConfiguration> getNamespacedSources() {
            if (!Objects.equals(lastLang, currentLang)) {
                reload();
            }

            List<FileConfiguration> sources = new ArrayList<>();
            addIfNotEmpty(langCustomOverrides, sources);
            addIfNotEmpty(namespacedSelected, sources);
            addIfNotEmpty(namespacedDefault, sources);
            return sources;
        }
    }
}