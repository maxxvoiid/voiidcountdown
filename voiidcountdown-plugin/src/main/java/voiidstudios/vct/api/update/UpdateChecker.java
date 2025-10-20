package voiidstudios.vct.api.update;

import org.bukkit.event.Listener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UpdateChecker implements Listener {
    private static final String API_URL = "https://api.github.com/repos/Voiid-Studios/voiidcountdown/releases/latest";
    private String currentVersion;
    private String latestVersion;

    public UpdateChecker(String currentVersion){
        this.currentVersion = currentVersion;
    }

    public UpdateCheckerResult check(){
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(API_URL).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);

            JsonObject release = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonObject();

            if (release.get("prerelease").getAsBoolean()) return UpdateCheckerResult.noErrors(null);

            String tag = release.get("tag_name").getAsString().replace("v", "").trim();
            if (!tag.startsWith("2")) return UpdateCheckerResult.noErrors(null);

            latestVersion = tag;

            if (!latestVersion.equalsIgnoreCase(currentVersion)) return UpdateCheckerResult.noErrors(latestVersion);

            return UpdateCheckerResult.noErrors(null);
        } catch (Exception ex) {
            return UpdateCheckerResult.error();
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }
}
