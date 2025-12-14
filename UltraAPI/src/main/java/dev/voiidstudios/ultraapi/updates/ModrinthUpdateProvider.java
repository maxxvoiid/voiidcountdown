package dev.voiidstudios.ultraapi.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.bukkit.plugin.Plugin;

public class ModrinthUpdateProvider implements UpdateProvider {

    private static final String API_URL = "https://api.modrinth.com/v2/project/%s/version";

    @Override
    public String getName() {
        return "modrinth";
    }

    @Override
    public UpdateCheckResult check(Plugin plugin, String projectId) throws Exception {
        URL url = new URL(String.format(API_URL, projectId));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", "UltraAPI-UpdateChecker");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        JsonElement root = JsonParser.parseReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        if (!root.isJsonArray()) {
            throw new IllegalStateException("Unexpected response when requesting versions");
        }

        JsonArray versions = root.getAsJsonArray();
        if (versions.size() == 0) {
            return UpdateCheckResult.noUpdate(getName(), plugin);
        }

        JsonObject latestRelease = findLatestRelease(versions);
        if (latestRelease == null) {
            return UpdateCheckResult.noUpdate(getName(), plugin);
        }

        String latestVersion = latestRelease.get("version_number").getAsString();
        String currentVersion = plugin.getDescription().getVersion();
        String downloadUrl = extractDownloadUrl(latestRelease);

        if (!latestVersion.equalsIgnoreCase(currentVersion)) {
            return UpdateCheckResult.updateAvailable(getName(), plugin, latestVersion, downloadUrl);
        }

        return UpdateCheckResult.noUpdate(getName(), plugin);
    }

    private JsonObject findLatestRelease(JsonArray versions) {
        for (JsonElement element : versions) {
            JsonObject version = element.getAsJsonObject();
            if (version.has("version_type") && "release".equalsIgnoreCase(version.get("version_type").getAsString())) {
                return version;
            }
        }
        return versions.get(0).getAsJsonObject();
    }

    private String extractDownloadUrl(JsonObject release) {
        if (!release.has("files") || !release.get("files").isJsonArray()) {
            return null;
        }
        JsonArray files = release.getAsJsonArray("files");
        if (files.size() == 0) {
            return null;
        }
        JsonObject primaryFile = files.get(0).getAsJsonObject();
        if (primaryFile.has("url")) {
            return primaryFile.get("url").getAsString();
        }
        if (primaryFile.has("filename") && primaryFile.has("downloads")) {
            JsonArray downloads = primaryFile.getAsJsonArray("downloads");
            if (!downloads.isJsonNull() && downloads.size() > 0) {
                return downloads.get(0).getAsString();
            }
        }
        return null;
    }
}
