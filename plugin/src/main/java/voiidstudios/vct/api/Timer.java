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
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TimerManager;
import voiidstudios.vct.utils.Formatter;
import voiidstudios.vct.utils.ServerCompatibility;
import voiidstudios.vct.configs.model.TimerConfig;

public class Timer implements Runnable {
    private final MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();

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

    public int getInitialSeconds() {
        return this.initialSeconds;
    }

    public int getRemainingSeconds() {
        return this.seconds;
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

    private void startTask(int seconds) {
        final int increment = -1;

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

                    double progress = (double) Timer.this.seconds / (double) Timer.this.initialSeconds;
                    progress = Math.max(0.0, Math.min(1.0, progress));
                    Timer.this.bossbar.setProgress(progress);
                }

                if (Timer.this.seconds <= 0) {
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
        this.seconds = seconds;
    }

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

    private String formatTimeHH(long time) {
        long hours = time / 3600L;
        return String.format("%02d", hours);
    }

    private String formatTimeMM(long time) {
        long minutes = time % 3600L / 60L;
        return String.format("%02d", minutes);
    }

    private String formatTimeSS(long time) {
        long seconds = time % 60L;
        return String.format("%02d", seconds);
    }

    public String getTimerId() {
        return timerId;
    }

    public String getFormat() {
        return format;
    }

    public String getInitialTime() {
        return formatTime(this.initialSeconds);
    }

    public String getTimeLeft() {
        return formatTime(this.seconds);
    }

    public String getTimeLeftHH() {
        return formatTimeHH(this.seconds);
    }

    public String getTimeLeftHHDigit1() {
        return splitDigits(formatTimeHH(this.seconds))[0];
    }

    public String getTimeLeftHHDigit2() {
        return splitDigits(formatTimeHH(this.seconds))[1];
    }

    public String getTimeLeftMM() {
        return formatTimeMM(this.seconds);
    }

    public String getTimeLeftMMDigit1() {
        return splitDigits(formatTimeMM(this.seconds))[0];
    }

    public String getTimeLeftMMDigit2() {
        return splitDigits(formatTimeMM(this.seconds))[1];
    }

    public String getTimeLeftSS() {
        return formatTimeSS(this.seconds);
    }

    public String getTimeLeftSSDigit1() {
        return splitDigits(formatTimeSS(this.seconds))[0];
    }

    public String getTimeLeftSSDigit2() {
        return splitDigits(formatTimeSS(this.seconds))[1];
    }

    public boolean isActive() {
        return task != null;
    }

    public boolean isPaused() {
        return this.task == null && this.seconds > 0;
    }

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
        if (!"COUNTDOWN".equals(format)) {
            return;
        }
        stop();
    }

    public void resume() {
        if (!"COUNTDOWN".equals(format)) return;
        if (this.task == null) startTask(this.seconds);
    }

    public void run() {
        this.seconds--;
        for (Player player : Bukkit.getOnlinePlayers())
            this.bossbar.addPlayer(player);

        if (this.seconds <= 0) {
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