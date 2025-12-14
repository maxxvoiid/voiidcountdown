package dev.voiidstudios.ultraapi.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.bukkit.plugin.Plugin;

public class GithubReleaseUpdateProvider implements UpdateProvider {

    private static final String API_URL = "https://api.github.com/repos/%s/releases/latest";

    @Override
    public String getName() {
        return "github";
    }

    @Override
    public UpdateCheckResult check(Plugin plugin, String projectId) throws Exception {
        URL url = new URL(String.format(API_URL, projectId));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("User-Agent", plugin.getName() + "-UpdateChecker");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        JsonObject release = JsonParser.parseReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)).getAsJsonObject();

        if (release.has("prerelease") && release.get("prerelease").getAsBoolean()) {
            return UpdateCheckResult.noUpdate(getName(), plugin);
        }

        String tag = release.get("tag_name").getAsString().replace("v", "").trim();
        String currentVersion = plugin.getDescription().getVersion();
        String downloadUrl = extractDownloadUrl(release);

        if (!tag.equalsIgnoreCase(currentVersion)) {
            return UpdateCheckResult.updateAvailable(getName(), plugin, tag, downloadUrl);
        }

        return UpdateCheckResult.noUpdate(getName(), plugin);
    }

    private String extractDownloadUrl(JsonObject release) {
        if (!release.has("assets")) {
            return null;
        }

        JsonArray assets = release.getAsJsonArray("assets");
        if (assets.size() == 0) {
            return null;
        }

        JsonObject asset = assets.get(0).getAsJsonObject();
        if (asset.has("browser_download_url")) {
            return asset.get("browser_download_url").getAsString();
        }
        return null;
    }
}
