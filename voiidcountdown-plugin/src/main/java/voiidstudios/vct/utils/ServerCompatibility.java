package voiidstudios.vct.utils;

import org.bukkit.Bukkit;

import voiidstudios.vct.managers.MessagesManager;

public class ServerCompatibility {
    private static Boolean folia = null;

    public static boolean isFolia() {
        if (folia != null) return folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.scheduler.RegionScheduler");
            Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage("&eRunning in Folia-compatible mode — please report any issues on GitHub!"));
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        return folia;
    }
}
