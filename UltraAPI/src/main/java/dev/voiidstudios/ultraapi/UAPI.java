package dev.voiidstudios.ultraapi;

import dev.voiidstudios.ultraapi.config.UConfig;
import dev.voiidstudios.ultraapi.updates.UpdateService;
import org.bukkit.plugin.Plugin;

/**
 * Static facade that mirrors {@link UltraAPI} helpers for easier consumption from other plugins.
 */
public final class UAPI {

    private UAPI() {
    }

    public static UConfig config(Plugin plugin) {
        return UltraAPI.config("config.yml");
    }

    public static UConfig config(String fileName) {
        return UltraAPI.config(fileName);
    }

    public static UConfig config(Plugin plugin, String fileName) {
        return UltraAPI.config(plugin, fileName);
    }

    public static UpdateService updates() {
        return UltraAPI.updates();
    }
}
