package voiidstudios.vct.api;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.managers.TimerManager;
import voiidstudios.vct.utils.Formatter;
import voiidstudios.vct.utils.ServerCompatibility;
import voiidstudios.vct.configs.model.TimerConfig;

public class Timer implements Runnable {
    private int seconds;
    private final TimerMode mode;
    private final BossBar bossbar;
    private Object task;
    private boolean hasSound;
    private String soundFinalName;
    public float soundVolume;
    public float soundPitch;
    private String timerText;
    private int initialSeconds;
    private int refreshInterval;
    private final int maxValue = 359999;
    private final int minValue = 0;
    private final String timerId;
    private Integer targetSeconds;

    public Timer(int seconds, String timeText, String timeSound, BarColor barcolor, BarStyle barstyle, String timerId, boolean hasSoundd, float soundVolumee, float soundPitchh) {
        this(seconds, TimerMode.COUNTDOWN, timeText, timeSound, barcolor, barstyle, timerId, hasSoundd, soundVolumee, soundPitchh, null);
    }

    public Timer(int seconds, TimerMode mode, String timeText, String timeSound, BarColor barcolor, BarStyle barstyle, String timerId, boolean hasSoundd, float soundVolumee, float soundPitchh, Integer targetSeconds) {
        this.mode = mode == null ? TimerMode.COUNTDOWN : mode;
        this.seconds = seconds;
        this.initialSeconds = seconds;
        this.timerId = timerId;
        this.targetSeconds = normalizeTarget(targetSeconds);

        this.refreshInterval = VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getRefresh_ticks();
        this.hasSound = hasSoundd;

        this.timerText = timeText;
        this.soundFinalName = timeSound;
        this.soundVolume = soundVolumee;
        this.soundPitch = soundPitchh;

        this.bossbar = Bukkit.createBossBar("", barcolor, barstyle, new org.bukkit.boss.BarFlag[0]);
    }

    private Integer normalizeTarget(Integer target) {
        if (target == null) {
            return this.mode == TimerMode.COUNTDOWN ? 0 : null;
        }
        if (target < minValue) {
            return minValue;
        }
        return Math.min(target, maxValue);
    }

    public int getInitialSeconds() {
        return this.initialSeconds;
    }

    public int getRemainingSeconds() {
        if (this.mode == TimerMode.COUNTDOWN) {
            return this.seconds;
        }
        if (this.targetSeconds != null) {
            return Math.max(0, this.targetSeconds - this.seconds);
        }
        return this.seconds;
    }

    public int getCurrentSeconds() {
        return this.seconds;
    }

    public int getElapsedSeconds() {
        if (this.mode == TimerMode.COUNTDOWN) {
            return Math.max(0, this.initialSeconds - this.seconds);
        }
        return this.seconds;
    }

    public TimerMode getMode() {
        return this.mode;
    }

    public Integer getTargetSeconds() {
        return this.targetSeconds;
    }

    public void setTargetSeconds(Integer targetSeconds) {
        this.targetSeconds = normalizeTarget(targetSeconds);
    }

    public String getTimertext() {
        return this.timerText;
    }

    public String getTimertextFormated() {
        return this.timerText
                .replace("%HH%", formatTimeHH(this.seconds))
                .replace("%MM%", formatTimeMM(this.seconds))
                .replace("%SS%", formatTimeSS(this.seconds))
                .replace("%H1%", getTimeLeftHHDigit1())
                .replace("%H2%", getTimeLeftHHDigit2())
                .replace("%M1%", getTimeLeftMMDigit1())
                .replace("%M2%", getTimeLeftMMDigit2())
                .replace("%S1%", getTimeLeftSSDigit1())
                .replace("%S2%", getTimeLeftSSDigit2());
    }

    public static void playSound(Player player, String actionLine) {
        // playsound: sound;volume;pitch
        String[] sep = actionLine.split(";");
        String soundName = sep[0];
        float volume = Float.parseFloat(sep[1]);
        float pitch = Float.parseFloat(sep[2]);

        boolean success = false;

        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            player.playSound(player.getLocation(), sound, volume, pitch);
            success = true;
        } catch (IllegalArgumentException ignored) {
            try {
                player.playSound(player.getLocation(), soundName, volume, pitch);
                success = true;
            } catch (Exception e) { /* ignore */ }
        }

        if (!success) {
            Bukkit.getLogger().warning("[TuPlugin] No se pudo reproducir el sonido: " + soundName);
        }
    }

    private void updateBossBarTitle(String phasesText) {
        Formatter formatter = VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getFormatter();
        Object formatted = formatter.format(
                VoiidCountdownTimer.getInstance(),
                Bukkit.getConsoleSender(),
                phasesText
        );

        try {
            Class<?> componentClass = null;
            try {
                componentClass = Class.forName("net.kyori.adventure.text.Component");
            } catch (ClassNotFoundException ignored) {
                componentClass = null;
            }

            if (componentClass != null && componentClass.isInstance(formatted)) {
                try {
                    this.bossbar.getClass().getMethod("setTitle", componentClass).invoke(this.bossbar, formatted);
                    return;
                } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException e) {}

                try {
                    Class<?> legacyCls = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer");

                    Object serializer;
                    try {
                        serializer = legacyCls.getMethod("legacySection").invoke(null);
                    } catch (NoSuchMethodException nsme) {
                        Class<?> builderPublicClass = Class.forName("net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer$Builder");
                        Object builder = legacyCls.getMethod("builder").invoke(null);
                        builderPublicClass.getMethod("character", char.class).invoke(builder, '&');
                        try { builderPublicClass.getMethod("hexColors").invoke(builder); } catch (NoSuchMethodException ignored) {}
                        serializer = builderPublicClass.getMethod("build").invoke(builder);
                    }

                    String legacyTitle = (String) legacyCls.getMethod("serialize", componentClass).invoke(serializer, formatted);
                    this.bossbar.setTitle(legacyTitle);
                    return;
                } catch (Throwable t) {}
            }
        } catch (Throwable t) {}

        if (formatted instanceof String) {
            this.bossbar.setTitle(((String) formatted).replace('&', '§'));
        } else {
            this.bossbar.setTitle(phasesText.replace('&', '§'));
        }
    }

    private void startTask() {
        final int increment = this.mode.getDirection();

        Runnable taskRunnable = new Runnable() {
            private int tickCounter = 0;
            private int refreshCounter = 0;

            @Override
            public void run() {
                tickCounter++;
                refreshCounter++;

                if (tickCounter >= 20) {
                    tickCounter = 0;
                    Timer.this.seconds += increment;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Timer.this.bossbar.addPlayer(player);

                        if (Timer.this.hasSound && Timer.this.soundFinalName != null) {
                            playSound(player, soundFinalName + ";" + soundVolume + ";" + soundPitch);
                        }
                    }

                    Bukkit.getPluginManager().callEvent(new VCTEvent(Timer.this, VCTEvent.VCTEventType.CHANGE, null));
                }

                if (refreshCounter >= Timer.this.refreshInterval) {
                    refreshCounter = 0;

                    String rawText = Timer.this.getTimertextFormated();
                    String phasesText = VoiidCountdownTimer.getPhasesManager().formatPhases(rawText);
                    updateBossBarTitle(phasesText);

                    Timer.this.bossbar.setProgress(calculateProgress());
                }

                if (Timer.this.mode.shouldFinish(Timer.this.seconds, Timer.this.targetSeconds)) {
                    stop();
                    Bukkit.getPluginManager().callEvent(new VCTEvent(Timer.this, VCTEvent.VCTEventType.FINISH, null));
                    if (ServerCompatibility.isFolia()) {
                        Bukkit.getGlobalRegionScheduler().runDelayed(VoiidCountdownTimer.getInstance(), scheduledTask ->
                            Timer.this.bossbar.removeAll(),
                            VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getTicks_hide_after_ending()
                        );
                    } else {
                        Bukkit.getScheduler().runTaskLater(VoiidCountdownTimer.getInstance(), () ->
                            Timer.this.bossbar.removeAll(),
                            VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getTicks_hide_after_ending()
                        );
                    }
                }
            }
        };

        if (ServerCompatibility.isFolia()) { // folia
            Object scheduledTask = Bukkit.getGlobalRegionScheduler().runAtFixedRate(
                VoiidCountdownTimer.getInstance(),
                ts -> taskRunnable.run(),
                1L, 1L
            );
            this.task = scheduledTask;
        } else {
            this.task = Bukkit.getScheduler().runTaskTimer(
                VoiidCountdownTimer.getInstance(),
                taskRunnable,
                1L, 1L
            );
        }
    }

    private double calculateProgress() {
        if (this.mode == TimerMode.COUNTDOWN) {
            if (this.initialSeconds <= 0) {
                return 0.0;
            }
            double progress = (double) this.seconds / (double) this.initialSeconds;
            return Math.max(0.0, Math.min(1.0, progress));
        }

        int baseline = 0;
        if (this.targetSeconds != null && this.targetSeconds > 0) {
            baseline = this.targetSeconds;
        } else {
            baseline = Math.max(this.initialSeconds, this.seconds);
        }

        if (baseline <= 0) {
            return 0.0;
        }

        double progress = (double) this.seconds / (double) baseline;
        return Math.max(0.0, Math.min(1.0, progress));
    }

    public static void refreshTimerText() {
        Timer current = TimerManager.getInstance().getTimer();
        if (current == null) return;

        current.refreshInterval = VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getRefresh_ticks();

        if (current.timerId != null) {
            try {
                TimerConfig cfg = VoiidCountdownTimer.getConfigsManager().getTimerConfig(current.timerId);
                if (cfg != null && cfg.isEnabled()) {
                    current.timerText = cfg.getText();
                    current.hasSound = cfg.isSoundEnabled();
                    current.soundVolume = cfg.getSoundVolume();
                    current.soundPitch = cfg.getSoundPitch();
                    try { current.soundFinalName = cfg.getSound(); } catch (Exception ignored) { /* keep existing */ }
                    try { current.bossbar.setColor(cfg.getColor()); } catch (Exception ignored) {}
                    return;
                }
            } catch (Throwable t) {}
        }

        current.timerText = "%HH%:%MM%:%SS%";
        current.soundFinalName = "UI_BUTTON_CLICK";
        current.hasSound = false;
        current.soundVolume = 1.0f;
        current.soundPitch = 1.0f;

        try {
            BarColor color = BarColor.WHITE;
            current.bossbar.setColor(color);
        } catch (Exception ignored) {}
    }

    public void setBossBarColor(BarColor color) {
        this.bossbar.setColor(color);
    }

    public void setBossBarStyle(BarStyle style) {
        this.bossbar.setStyle(style);
    }

    public void setSeconds(int seconds) {
        this.seconds = Math.max(this.minValue, Math.min(this.maxValue, seconds));
    }

    public void restoreState(int initialSeconds, int currentSeconds, Integer targetSeconds) {
        this.initialSeconds = Math.max(this.minValue, Math.min(this.maxValue, initialSeconds));
        this.seconds = Math.max(this.minValue, Math.min(this.maxValue, currentSeconds));
        setTargetSeconds(targetSeconds);
    }

    private String[] splitDigits(String value) {
        if (value == null || value.length() < 2) return new String[]{"0", "0"};
        return new String[]{String.valueOf(value.charAt(0)), String.valueOf(value.charAt(1))};
    }

    private String formatTime(long time) {
        time = Math.max(0, time);
        long hours = time / 3600L;
        long minutes = time % 3600L / 60L;
        long seconds = time % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatTimeHH(long time) {
        time = Math.max(0, time);
        long hours = time / 3600L;
        return String.format("%02d", hours);
    }

    private String formatTimeMM(long time) {
        time = Math.max(0, time);
        long minutes = time % 3600L / 60L;
        return String.format("%02d", minutes);
    }

    private String formatTimeSS(long time) {
        time = Math.max(0, time);
        long seconds = time % 60L;
        return String.format("%02d", seconds);
    }

    public String getTimerId() {
        return timerId;
    }

    public String getInitialTime() {
        return formatTime(this.initialSeconds);
    }

    public String getTimeLeft() {
        return formatTime(getRemainingSeconds());
    }

    public String getTimeLeftHH() {
        return formatTimeHH(getRemainingSeconds());
    }

    public String getTimeLeftHHDigit1() {
        return splitDigits(formatTimeHH(getRemainingSeconds()))[0];
    }

    public String getTimeLeftHHDigit2() {
        return splitDigits(formatTimeHH(getRemainingSeconds()))[1];
    }

    public String getTimeLeftMM() {
        return formatTimeMM(getRemainingSeconds());
    }

    public String getTimeLeftMMDigit1() {
        return splitDigits(formatTimeMM(getRemainingSeconds()))[0];
    }

    public String getTimeLeftMMDigit2() {
        return splitDigits(formatTimeMM(getRemainingSeconds()))[1];
    }

    public String getTimeLeftSS() {
        return formatTimeSS(getRemainingSeconds());
    }

    public String getTimeLeftSSDigit1() {
        return splitDigits(formatTimeSS(getRemainingSeconds()))[0];
    }

    public String getTimeLeftSSDigit2() {
        return splitDigits(formatTimeSS(getRemainingSeconds()))[1];
    }

    public boolean isActive() {
        return task != null;
    }

    public boolean isPaused() {
        if (this.task != null) {
            return false;
        }
        if (this.mode == TimerMode.COUNTDOWN) {
            return this.seconds > 0;
        }
        return true;
    }

    public void start() {
        if (task != null) {
            stop();
        }
        startTask();
    }

    public void stop() {
        if (task != null) {
            try {
                if (ServerCompatibility.isFolia()) {
                    ((ScheduledTask) task).cancel();
                } else {
                    ((BukkitTask) task).cancel();
                }
            } catch (Exception ignored) {}
            task = null;
        }

        if (this.bossbar != null)
            this.bossbar.removeAll();
    }

    public void add(int addSeconds) {
        if (addSeconds <= 0) {
            return;
        }

        if (this.mode == TimerMode.COUNTDOWN) {
            if (this.seconds + addSeconds > this.maxValue) {
                this.seconds = this.maxValue;
                this.initialSeconds = this.maxValue;
            } else {
                this.seconds += addSeconds;
                this.initialSeconds += addSeconds;
            }
        } else {
            this.seconds = Math.min(this.maxValue, this.seconds + addSeconds);
        }
    }

    public void set(int setSeconds) {
        int clamped = Math.max(this.minValue, Math.min(this.maxValue, setSeconds));
        this.seconds = clamped;

        if (this.mode == TimerMode.COUNTDOWN) {
            this.initialSeconds = clamped;
        } else if (this.targetSeconds != null && this.targetSeconds < this.seconds) {
            this.targetSeconds = this.seconds;
        }
    }

    public void take(int takeSeconds) {
        if (takeSeconds <= 0) {
            return;
        }

        if (this.mode == TimerMode.COUNTDOWN) {
            if (this.seconds - takeSeconds >= this.minValue) {
                this.seconds -= takeSeconds;
                this.initialSeconds = Math.max(this.seconds, this.initialSeconds - takeSeconds);
            }
        } else {
            this.seconds = Math.max(this.minValue, this.seconds - takeSeconds);
        }
    }

    public void pause() {
        stop();
    }

    public void resume() {
        if (this.task == null) startTask();
    }

    public void run() {
        this.seconds += this.mode.getDirection();
        for (Player player : Bukkit.getOnlinePlayers())
            this.bossbar.addPlayer(player);

        if (this.mode.shouldFinish(this.seconds, this.targetSeconds)) {
            if (this.task != null) {
                if (ServerCompatibility.isFolia()) { // Folia
                    try {
                        this.task.getClass().getMethod("cancel").invoke(this.task);
                    } catch (Exception ignored) {}
                } else { // Bukkit
                    Bukkit.getScheduler().cancelTask(((BukkitTask) this.task).getTaskId());
                }
            }

            this.bossbar.removeAll();
            TimerManager.getInstance().removeTimer();
        }
    }
}