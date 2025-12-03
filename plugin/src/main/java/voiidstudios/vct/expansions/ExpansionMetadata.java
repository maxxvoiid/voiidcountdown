package voiidstudios.vct.expansions;

import org.bukkit.configuration.file.YamlConfiguration;

import voiidstudios.vct.expansions.exceptions.InvalidExpansionException;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExpansionMetadata {
    private final String name;
    private final String mainScript;
    private final String version;
    private final List<String> authors;
    private final String description;
    private final boolean enabled;

    private ExpansionMetadata(String name, String mainScript, String version, List<String> authors, String description, boolean enabled) {
        this.name = name;
        this.mainScript = mainScript;
        this.version = version;
        this.authors = authors == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(authors));
        this.description = description;
        this.enabled = enabled;
    }

    public String getName() {
        return name;
    }

    public String getMainScript() {
        return mainScript;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public static ExpansionMetadata fromFile(File metadataFile) throws InvalidExpansionException {
        if (metadataFile == null || !metadataFile.exists()) {
            throw new InvalidExpansionException("Missing expansion metadata file");
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(metadataFile);

        String name = configuration.getString("name");
        String main = configuration.getString("main");
        String version = configuration.getString("version", "1.0.0");
        String description = configuration.getString("description", "");
        boolean enabled = configuration.getBoolean("enabled", true);

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidExpansionException("Expansion metadata is missing the 'name' property");
        }

        if (main == null || main.trim().isEmpty()) {
            throw new InvalidExpansionException("Expansion metadata is missing the 'main' script file");
        }

        List<String> authors = configuration.getStringList("authors");

        return new ExpansionMetadata(
            name.trim(),
            main.trim(),
            version == null ? "1.0.0" : version.trim(),
            authors,
            description,
            enabled
        );
    }
}