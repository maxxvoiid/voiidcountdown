package voiidstudios.vct.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.managers.MessagesManager;

public class PlayerListener implements Listener {
    private final VoiidCountdownTimer plugin;

    public PlayerListener(VoiidCountdownTimer plugin){
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();

        // Update notification
        String latestVersion = plugin.getUpdateChecker().getLatestVersion();
        if ((player.isOp() || player.hasPermission("voiidcountdowntimer.admin"))
                && latestVersion != null
                && !plugin.version.equalsIgnoreCase(latestVersion)
                && VoiidCountdownTimer.getConfigsManager().getMainConfigManager().isUpdate_notification()) {
                    
            Map<String, String> repl = new HashMap<>();
            repl.put("%LATEST%", latestVersion);
            repl.put("%UPDATELINK%", "https://modrinth.com/datapack/voiid-countdown-timer");
                                                                                                        // Honestly, I prefer to leave the link hardcoded because
                                                                                                        // there may be bad people who change the URL in the YML to scam 
                                                                                                        // people, and this would slow them down a bit.

            msgManager.sendList(player, "system.update.available", true, repl);
        }
    }
}
