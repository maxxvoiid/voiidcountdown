package voiidstudios.vct.api.timerinternal;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Locale;

public final class SoundUtil {
    private SoundUtil() {}

    public static void playSound(Player player, String actionLine) {
        String[] sep = actionLine.split(";");
        String soundName = sep[0];
        float volume = Float.parseFloat(sep[1]);
        float pitch = Float.parseFloat(sep[2]);
        playSound(player, soundName, volume, pitch);
    }

    public static void playSound(Player player, String soundName, float volume, float pitch) {
        boolean success = false;
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase(Locale.ROOT));
            player.playSound(player.getLocation(), sound, volume, pitch);
            success = true;
        } catch (IllegalArgumentException ignored) {
            try {
                player.playSound(player.getLocation(), soundName, volume, pitch);
                success = true;
            } catch (Exception ignored2) {}
        }

        if (!success) {
            Bukkit.getLogger().warning("[TuPlugin] No se pudo reproducir el sonido: " + soundName);
        }
    }
}
