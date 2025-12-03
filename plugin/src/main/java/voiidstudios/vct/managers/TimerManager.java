package voiidstudios.vct.managers;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.VCTEvent;

public class TimerManager {
    private static TimerManager instance;
    private Timer timer;

    private TimerManager() {}

    public static TimerManager getInstance() {
        if (instance == null) instance = new TimerManager();
        return instance;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public Timer getTimer() {
        return this.timer;
    }

    public void removeTimer() {
        if (this.timer != null) {
            this.timer.stop();
            this.timer = null;
        }
    }

    public void finishTimer() {
        if (this.timer != null) {
            Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.FINISH, null));
        }
    }

    public void deleteTimer(@Nullable CommandSender sender) {
        MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();

        if (this.timer != null) {
            this.timer.stop();
            this.timer = null;

            if (sender != null) {
                msgManager.send(sender, "timer_status.stop");
            }
        } else {
            if (sender != null) {
                msgManager.send(sender, "timer_status.not_exists");
            }
        }
    }
}
