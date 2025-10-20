package voiidstudios.vct.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.TimerMode;
import voiidstudios.vct.configs.model.CustomConfig;
import voiidstudios.vct.utils.TimerDefaults;

public class TimerStateManager {
    private final CustomConfig stateConfig;

    public TimerStateManager(VoiidCountdownTimer plugin) {
        this.stateConfig = new CustomConfig("timer_state.yml", plugin, null, true);
        this.stateConfig.registerConfig();
    }

    public void saveState() {
        FileConfiguration cfg = stateConfig.getConfig();

        Timer timer = TimerManager.getInstance().getTimer();

        if (timer == null) {
            cfg.set("active", false);
            cfg.set("timer_id", null);
            cfg.set("initial", null);
            cfg.set("remaining", null);
            cfg.set("paused", null);
            stateConfig.saveConfig();
            return;
        }

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix+"&rSaving the state of timer " + timer.getTimerId()));

        cfg.set("active", true);
        cfg.set("timer_id", timer.getTimerId());
        cfg.set("initial", timer.getInitialSeconds());
        if (timer.getMode() == TimerMode.COUNTDOWN) {
            cfg.set("remaining", timer.getRemainingSeconds());
        } else {
            cfg.set("remaining", timer.getCurrentSeconds());
        }
        cfg.set("paused", timer.isPaused());
        cfg.set("mode", timer.getMode().name());
        cfg.set("target", timer.getTargetSeconds());
        cfg.set("elapsed", timer.getElapsedSeconds());
        stateConfig.saveConfig();
    }

    public void loadState() {
        FileConfiguration cfg = stateConfig.getConfig();

        if (!cfg.contains("active") || !cfg.getBoolean("active", false)) return;

        String savedId = cfg.getString("timer_id", null);
        int initial = cfg.getInt("initial", -1);
        int remaining = cfg.getInt("remaining", -1);
        boolean paused = cfg.getBoolean("paused", false);
        TimerDefaults.TimerSettings settings = TimerDefaults.getSettings(savedId);
        TimerMode mode = settings.mode;
        String storedMode = cfg.getString("mode", null);
        if (storedMode != null) {
            try {
                mode = TimerMode.valueOf(storedMode);
            } catch (IllegalArgumentException ignored) { /* keep default */ }
        }
        Integer target = cfg.isInt("target") ? cfg.getInt("target") : settings.targetSeconds;

        if (initial < 0 || remaining < 0) return;

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix+"&rLoading the state of timer " + savedId));
        String usedId = settings.id != null ? settings.id : savedId;

        Timer timer = new Timer(
                initial,
                mode,
                settings.text,
                settings.sound,
                settings.color,
                settings.style,
                usedId,
                settings.hasSound,
                settings.volume,
                settings.pitch,
                target
        );

        TimerManager.getInstance().removeTimer();
        timer.restoreState(initial, remaining, target);
        TimerManager.getInstance().setTimer(timer);

        String stateDetails;
        if (mode == TimerMode.COUNTDOWN) {
            stateDetails = remaining + "/" + initial + " seconds";
        } else {
            stateDetails = remaining + " seconds elapsed";
        }

        Bukkit.getConsoleSender().sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix+"&aLoaded the state of timer " + savedId + " &e(" + stateDetails + " | Mode: " + mode + " | Paused: " + paused + ")"));

        if (!paused) {
            timer.start();
        }
    }
}