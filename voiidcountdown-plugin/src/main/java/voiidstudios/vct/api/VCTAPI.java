package voiidstudios.vct.api;

import org.bukkit.Bukkit;

import org.bukkit.plugin.java.JavaPlugin;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.addon.VCTAddon;

public class VCTAPI {
    public static boolean isAvailable() {
        return Bukkit.getPluginManager().getPlugin("VoiidCountdownTimer") != null
                && VoiidCountdownTimer.getInstance() != null
                && VoiidCountdownTimer.getInstance().isEnabled();
    }

    public static boolean registerAddon(String id, VCTAddon addon, JavaPlugin provider) {
        if (!isAvailable()) return false;
        if (VoiidCountdownTimer.getAddonManager() == null) return false;
        return VoiidCountdownTimer.getAddonManager().registerAddon(id, addon, provider);
    }

    public static boolean unregisterAddon(String id) {
        if (!isAvailable()) return false;
        if (VoiidCountdownTimer.getAddonManager() == null) return false;
        return VoiidCountdownTimer.getAddonManager().unregisterAddon(id);
    }
}