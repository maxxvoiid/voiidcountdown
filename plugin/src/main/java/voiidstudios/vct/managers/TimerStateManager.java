package voiidstudios.vct.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.configs.model.CustomConfig;
import voiidstudios.vct.utils.TimerDefaults;

public class TimerStateManager {
    private final CustomConfig stateConfig;
    private final MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();

    public TimerStateManager(VoiidCountdownTimer plugin) {
        this.stateConfig = new CustomConfig("timer_state.yml", plugin, "core", true);
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

        msgManager.console(VoiidCountdownTimer.prefix+"&rSaving the state of timer " + timer.getTimerId());

        cfg.set("active", true);
        cfg.set("timer_id", timer.getTimerId());
        cfg.set("initial", timer.getInitialSeconds());
        cfg.set("remaining", timer.getRemainingSeconds());
        cfg.set("paused", timer.isPaused());
        stateConfig.saveConfig();
    }

    public void loadState() {
        FileConfiguration cfg = stateConfig.getConfig();

        if (!cfg.contains("active") || !cfg.getBoolean("active", false)) return;

        String savedId = cfg.getString("timer_id", null);
        int initial = cfg.getInt("initial", -1);
        int remaining = cfg.getInt("remaining", -1);
        boolean paused = cfg.getBoolean("paused", false);

        if (initial <= 0 || remaining <= 0) return;

        msgManager.console(VoiidCountdownTimer.prefix+"&rLoading the state of timer " + savedId);
        TimerDefaults.TimerSettings settings = TimerDefaults.getSettings(savedId);
        String usedId = savedId;

        Timer timer = new Timer(
                initial,
                settings.text,
                settings.sound,
                settings.color,
                settings.style,
                settings.format,
                usedId,
                settings.hasSound,
                settings.volume,
                settings.pitch
        );

        TimerManager.getInstance().removeTimer();
        timer.setSeconds(remaining);
        TimerManager.getInstance().setTimer(timer);

        if ("COUNTDOWN".equals(settings.format)) {
            msgManager.console(VoiidCountdownTimer.prefix+"&aLoaded the state of timer " + savedId + " &e(" + remaining + "/" + initial + " seconds | Paused: " + paused + ")");
        } else {
            msgManager.debug("&4There is a timer state, but it does not have the COUNTDOWN format, skipping...");
        }

        if (!paused && "COUNTDOWN".equals(settings.format)) {
            timer.start();
        }
    }
}