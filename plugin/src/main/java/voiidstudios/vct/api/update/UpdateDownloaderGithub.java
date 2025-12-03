package voiidstudios.vct.api.update;

import org.bukkit.Bukkit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.managers.MessagesManager;

import com.google.gson.JsonElement;

public class UpdateDownloaderGithub {
    private static final String API_URL = "https://api.github.com/repos/Voiid-Studios/voiidcountdown/releases/latest";
    private static final String TARGET_JAR = "VoiidCountdownTimer.jar";
    private static final String USER_AGENT = "VoiidCountdownTimer-Updater";

    private static JsonObject getLatestReleaseJson() throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);

        if (conn.getResponseCode() != 200)
            throw new RuntimeException("GitHub API responded with code " + conn.getResponseCode());

        return JsonParser.parseReader(new InputStreamReader(conn.getInputStream())).getAsJsonObject();
    }

    private static String getDownloadUrlFromJson(JsonObject release) {
        if (release.get("prerelease").getAsBoolean()) return null;

        String tag = release.get("tag_name").getAsString().replace("v", "").trim();
        if (!tag.startsWith("2")) return null; // Only download plugin releases (2.x.x)

        for (JsonElement e : release.getAsJsonArray("assets")) {
            JsonObject asset = e.getAsJsonObject();
            if (asset.get("name").getAsString().equalsIgnoreCase(TARGET_JAR)) {
                return asset.get("browser_download_url").getAsString();
            }
        }
        return null;
    }

    private static void download(String url) throws Exception {
        Path updateFile = Bukkit.getUpdateFolderFile().toPath().resolve(TARGET_JAR);
        Files.createDirectories(updateFile.getParent());

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", USER_AGENT);

        try (InputStream in = conn.getInputStream()) {
            Files.copy(in, updateFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public static boolean downloadUpdate() {
        MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();

        try {
            JsonObject release = getLatestReleaseJson();
            String downloadUrl = getDownloadUrlFromJson(release);

            VoiidCountdownTimer plugin = VoiidCountdownTimer.getPlugin(VoiidCountdownTimer.class); 
            String latestVersion = plugin.getUpdateChecker().getLatestVersion();
            String currentVersion = plugin.getUpdateChecker().getCurrentVersion();

            if (downloadUrl == null) return false;

            msgManager.console(VoiidCountdownTimer.prefix + "&bDownloading latest stable update...");

            long start = System.currentTimeMillis();
            download(downloadUrl);

            long elapsed = System.currentTimeMillis() - start;
            msgManager.console(VoiidCountdownTimer.prefix + "&aDownloaded update in " + elapsed + "ms");
            msgManager.console(VoiidCountdownTimer.prefix + "&aVoiid Countdown Timer will be updated from &c" + currentVersion + " &a-> &e" + latestVersion +" &aon the next server restart!");

            return true;
        } catch (Exception ex) {
            msgManager.console(VoiidCountdownTimer.prefix + "&cFailed to download update: " + ex.getMessage());
            return false;
        }
    }
}
