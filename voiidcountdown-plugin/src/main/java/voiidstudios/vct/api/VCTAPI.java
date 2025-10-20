package voiidstudios.vct.api;

import org.bukkit.Bukkit;

import voiidstudios.vct.VoiidCountdownTimer;

public class VCTAPI {
    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("VoiidCountdownTimer") != null
                && VoiidCountdownTimer.getInstance() != null
                && VoiidCountdownTimer.getInstance().isEnabled();
    }
}