package voiidstudios.vct.api;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.timerinternal.BossBarTitleRenderer;
import voiidstudios.vct.api.timerinternal.SchedulerAdapter;
import voiidstudios.vct.api.timerinternal.SchedulerAdapters;
import voiidstudios.vct.api.timerinternal.SoundUtil;
import voiidstudios.vct.api.timerinternal.TimerTextFormatter;
import voiidstudios.vct.configs.model.TimerConfig;
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TimerManager;
import voiidstudios.vct.utils.Formatter;
import voiidstudios.vct.utils.ServerCompatibility;

public class Timer implements Runnable {
    private final MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();
    private final TimerTextFormatter textFormatter = new TimerTextFormatter();
    private final BossBarTitleRenderer titleRenderer = new BossBarTitleRenderer();
    private final SchedulerAdapter schedulerAdapter = SchedulerAdapters.createDefault();

    private int seconds;
    private final BossBar bossbar;
    private Object task;
    private boolean hasSound;
    private String soundFinalName;
    public float soundVolume;
    public float soundPitch;
    private final String format;
    private String timerText;
    private int initialSeconds;
    private int refreshInterval;
    private final int maxValue = 359999;
    private final int minValue = 0;
    private final String timerId;

    public Timer(int seconds, String timeText, String timeSound, BarColor barcolor, BarStyle barstyle, String format, String timerId, boolean hasSoundd, float soundVolumee, float soundPitchh) {
        this.seconds = seconds;
        this.initialSeconds = seconds;
        this.timerId = timerId;
        this.format = format;
        this.refreshInterval = VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getRefresh_ticks();
        this.hasSound = hasSoundd;
        this.timerText = timeText;
        this.soundFinalName = timeSound;
        this.soundVolume = soundVolumee;
        this.soundPitch = soundPitchh;
        this.bossbar = Bukkit.createBossBar("", barcolor, barstyle, new org.bukkit.boss.BarFlag[0]);
    }

    public int getInitialSeconds() { return this.initialSeconds; }
    public int getRemainingSeconds() { return this.seconds; }
    public String getTimertext() { return this.timerText; }

    public String getTimertextFormated() {
        return textFormatter.formatTimerText(this.timerText, this.seconds);
    }

    public static void playSound(Player player, String actionLine) {
        SoundUtil.playSound(player, actionLine);
    }

    private void updateBossBarTitle(String phasesText) {
        Formatter formatter = VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getFormatter();
        Object formatted = formatter.format(VoiidCountdownTimer.getInstance(), Bukkit.getConsoleSender(), phasesText);
        titleRenderer.setTitle(this.bossbar, formatted, phasesText);
    }

    private void startTask(int seconds) {
        final int increment = -1;
        Runnable taskRunnable = new Runnable() {
            private int tickCounter = 0;
            private int refreshCounter = 0;

            public void run() {
                tickCounter++;
                refreshCounter++;

                if (tickCounter >= 20) {
                    tickCounter = 0;
                    Timer.this.seconds += increment;

                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Timer.this.bossbar.addPlayer(player);
                        if (Timer.this.hasSound && Timer.this.soundFinalName != null) {
                            SoundUtil.playSound(player, soundFinalName, soundVolume, soundPitch);
                        }
                    }

                    Bukkit.getPluginManager().callEvent(new VCTEvent(Timer.this, VCTEvent.VCTEventType.CHANGE, null));
                }

                if (refreshCounter >= Timer.this.refreshInterval) {
                    refreshCounter = 0;
                    String rawText = Timer.this.getTimertextFormated();
                    String phasesText = VoiidCountdownTimer.getPhasesManager().formatPhases(rawText);
                    updateBossBarTitle(phasesText);

                    double progress = (double) Timer.this.seconds / (double) Timer.this.initialSeconds;
                    progress = Math.max(0.0, Math.min(1.0, progress));
                    Timer.this.bossbar.setProgress(progress);
                }

                if (Timer.this.seconds <= 0) {
                    stop();
                    Bukkit.getPluginManager().callEvent(new VCTEvent(Timer.this, VCTEvent.VCTEventType.FINISH, null));
                    schedulerAdapter.runDelayed(new Runnable() {
                        public void run() {
                            Timer.this.bossbar.removeAll();
                        }
                    }, VoiidCountdownTimer.getConfigsManager().getMainConfigManager().getTicks_hide_after_ending());
                }
            }
        };

        this.task = schedulerAdapter.runAtFixedRate(taskRunnable, 1L, 1L);
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
                    try { current.soundFinalName = cfg.getSound(); } catch (Exception ignored) {}
                    try { current.bossbar.setColor(cfg.getColor()); } catch (Exception ignored) {}
                    return;
                }
            } catch (Throwable ignored) {}
        }

        current.timerText = "%HH%:%MM%:%SS%";
        current.soundFinalName = "UI_BUTTON_CLICK";
        current.hasSound = false;
        current.soundVolume = 1.0f;
        current.soundPitch = 1.0f;

        try { current.bossbar.setColor(BarColor.WHITE); } catch (Exception ignored) {}
    }

    public void setBossBarColor(BarColor color) { this.bossbar.setColor(color); }
    public void setBossBarStyle(BarStyle style) { this.bossbar.setStyle(style); }
    public void setSeconds(int seconds) { this.seconds = seconds; }

    private String[] splitDigits(String value) {
        if (value == null || value.length() < 2) return new String[]{"0", "0"};
        return new String[]{String.valueOf(value.charAt(0)), String.valueOf(value.charAt(1))};
    }

    private String formatTime(long time) {
        long hours = time / 3600L;
        long minutes = time % 3600L / 60L;
        long seconds = time % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatTimeHH(long time) { return textFormatter.formatTimeHH(time); }
    private String formatTimeMM(long time) { return textFormatter.formatTimeMM(time); }
    private String formatTimeSS(long time) { return textFormatter.formatTimeSS(time); }

    public String getTimerId() { return timerId; }
    public String getFormat() { return format; }
    public String getInitialTime() { return formatTime(this.initialSeconds); }
    public String getTimeLeft() { return formatTime(this.seconds); }
    public String getTimeLeftHH() { return formatTimeHH(this.seconds); }
    public String getTimeLeftHHDigit1() { return splitDigits(formatTimeHH(this.seconds))[0]; }
    public String getTimeLeftHHDigit2() { return splitDigits(formatTimeHH(this.seconds))[1]; }
    public String getTimeLeftMM() { return formatTimeMM(this.seconds); }
    public String getTimeLeftMMDigit1() { return splitDigits(formatTimeMM(this.seconds))[0]; }
    public String getTimeLeftMMDigit2() { return splitDigits(formatTimeMM(this.seconds))[1]; }
    public String getTimeLeftSS() { return formatTimeSS(this.seconds); }
    public String getTimeLeftSSDigit1() { return splitDigits(formatTimeSS(this.seconds))[0]; }
    public String getTimeLeftSSDigit2() { return splitDigits(formatTimeSS(this.seconds))[1]; }

    public boolean isActive() { return task != null; }
    public boolean isPaused() { return this.task == null && this.seconds > 0; }

    public void start() {
        if (!"COUNTDOWN".equals(format)) {
            msgManager.debug("The timer chosen in the format " + format + ", VCT vanilla will NOT manage how that timer works, and external extensions will have to do so");
            msgManager.debug("If you want VCT vanilla to handle that timer as it normally does, use the COUNTDOWN format");
            return;
        }
        if (task != null) {
            stop();
        }
        startTask(this.seconds);
    }

    public void stop() {
        if (task != null) {
            try {
                schedulerAdapter.cancel(task);
            } catch (Exception ignored) {}
            task = null;
        }

        if (this.bossbar != null) this.bossbar.removeAll();
    }

    public void add(int addSeconds) {
        if (this.seconds + addSeconds > this.maxValue) {
            this.seconds = this.maxValue;
            this.initialSeconds = this.maxValue;
        } else {
            this.seconds += addSeconds;
            this.initialSeconds += addSeconds;
        }
    }

    public void set(int setSeconds) {
        if (setSeconds > this.maxValue) {
            this.seconds = this.maxValue;
            this.initialSeconds = this.maxValue;
        } else if (setSeconds < this.minValue) {
            this.seconds = this.minValue;
            this.initialSeconds = this.minValue;
        } else {
            this.seconds = setSeconds;
            this.initialSeconds = setSeconds;
        }
    }

    public void take(int takeSeconds) {
        if (this.seconds - takeSeconds >= this.minValue) {
            this.seconds -= takeSeconds;
            this.initialSeconds -= takeSeconds;
        }
    }

    public void pause() {
        if (!"COUNTDOWN".equals(format)) return;
        stop();
    }

    public void resume() {
        if (!"COUNTDOWN".equals(format)) return;
        if (this.task == null) startTask(this.seconds);
    }

    public void run() {
        this.seconds--;
        for (Player player : Bukkit.getOnlinePlayers()) this.bossbar.addPlayer(player);

        if (this.seconds <= 0) {
            if (this.task != null) {
                if (ServerCompatibility.isFolia()) {
                    try { this.task.getClass().getMethod("cancel").invoke(this.task); } catch (Exception ignored) {}
                } else {
                    Bukkit.getScheduler().cancelTask(((BukkitTask) this.task).getTaskId());
                }
            }

            this.bossbar.removeAll();
            TimerManager.getInstance().removeTimer();
        }
    }
}
