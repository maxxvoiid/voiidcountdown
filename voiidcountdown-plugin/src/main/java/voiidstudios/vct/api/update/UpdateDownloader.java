package voiidstudios.vct.api.update;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.bukkit.Bukkit;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.managers.MessagesManager;

public class UpdateDownloader {

    private static final String TARGET_JAR = "VoiidCountdownTimer.jar";
    private static final String USER_AGENT = "VoiidCountdownTimer-Updater";

    private UpdateDownloader() {
    }

    public static boolean download(String downloadUrl, String currentVersion, String latestVersion) {
        if (downloadUrl == null || downloadUrl.isEmpty()) {
            return false;
        }

        try {
            Path updateFile = Bukkit.getUpdateFolderFile().toPath().resolve(TARGET_JAR);
            Files.createDirectories(updateFile.getParent());

            HttpURLConnection conn = (HttpURLConnection) new URL(downloadUrl).openConnection();
            conn.setRequestProperty("User-Agent", USER_AGENT);

            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + "&bDownloading latest stable update..."));

            long start = System.currentTimeMillis();
            try (InputStream in = conn.getInputStream()) {
                Files.copy(in, updateFile, StandardCopyOption.REPLACE_EXISTING);
            }
            long elapsed = System.currentTimeMillis() - start;

            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + "&aDownloaded update in " + elapsed + "ms"));
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + "&aVoiid Countdown Timer will be updated from &c" + currentVersion + " &a-> &e" + latestVersion + " &aon the next server restart!"));
            return true;
        } catch (Exception ex) {
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix + "&cFailed to download update: " + ex.getMessage()));
            return false;
        }
    }
}
