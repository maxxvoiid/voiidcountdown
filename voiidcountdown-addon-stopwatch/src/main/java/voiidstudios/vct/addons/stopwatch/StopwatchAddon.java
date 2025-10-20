package voiidstudios.vct.addons.stopwatch;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.TimerMode;
import voiidstudios.vct.api.VCTActions;
import voiidstudios.vct.api.addon.VCTAddon;

public class StopwatchAddon implements VCTAddon {

    private final JavaPlugin provider;
    private VoiidCountdownTimer mainPlugin;

    public StopwatchAddon(JavaPlugin provider) {
        this.provider = provider;
    }

    @Override
    public void onRegister(VoiidCountdownTimer mainPlugin, JavaPlugin provider) {
        this.mainPlugin = mainPlugin;
    }

    @Override
    public void onUnregister() {
        this.mainPlugin = null;
    }

    public boolean isReady() {
        return mainPlugin != null && mainPlugin.isEnabled();
    }

    public Timer startStopwatch(@Nullable String timerId, @Nullable Integer targetSeconds, @Nullable CommandSender sender) {
        if (!isReady()) {
            return null;
        }
        return VCTActions.createStopwatch(timerId, targetSeconds, sender);
    }

    public Timer startStopwatch(@Nullable String timerId, @Nullable String targetHHMMSS, @Nullable CommandSender sender) {
        if (!isReady()) {
            return null;
        }
        return VCTActions.createStopwatch(timerId, targetHHMMSS, sender);
    }

    public Timer startTimer(String timeHHMMSS, @Nullable String timerId, TimerMode mode, @Nullable Integer targetSeconds, @Nullable CommandSender sender) {
        if (!isReady()) {
            return null;
        }
        return VCTActions.createTimer(timeHHMMSS, timerId, mode, targetSeconds, sender);
    }

    public boolean pauseTimer(@Nullable CommandSender sender) {
        return VCTActions.pauseTimer(sender);
    }

    public boolean resumeTimer(@Nullable CommandSender sender) {
        return VCTActions.resumeTimer(sender);
    }

    public void stopTimer(@Nullable CommandSender sender) {
        VCTActions.stopTimer(sender);
    }

    public JavaPlugin getProvider() {
        return provider;
    }

    @Nullable
    public VoiidCountdownTimer getMainPlugin() {
        return mainPlugin;
    }
}
