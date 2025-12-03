package voiidstudios.vct.utils;

import org.bukkit.Bukkit;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.managers.MessagesManager;

public class ServerCompatibility {
    private static final MessagesManager messagesManager = VoiidCountdownTimer.getMessagesManager();
    private static Boolean folia = null;

    public static boolean isFolia() {
        if (folia != null) return folia;

        try {
            Class.forName("io.papermc.paper.threadedregions.Region");
            folia = Bukkit.getServer().getName().equalsIgnoreCase("Folia");
        } catch (ClassNotFoundException e) {
            folia = false;
        }

        if (folia) {
            messagesManager.console("&eRunning in Folia-compatible mode — please report any issues on GitHub!");
        }

        return folia;
    }
}
