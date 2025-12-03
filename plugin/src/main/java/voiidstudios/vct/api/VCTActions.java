package voiidstudios.vct.api;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.model.TimerConfig;
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TimerManager;
import voiidstudios.vct.utils.TimerDefaults;

public class VCTActions {
    public static Timer createTimer(String timeHHMMSS, @Nullable String timerId, @Nullable CommandSender sender) {
        int totalSeconds = helper_parseTimeToSeconds(timeHHMMSS);
        if (totalSeconds <= 0) return null;

        String usedTimerId = (timerId == null || timerId.isEmpty()) ? "default" : timerId;
        TimerDefaults.TimerSettings settings = TimerDefaults.getSettings(usedTimerId);
        TimerManager.getInstance().removeTimer();

        Timer timer = new Timer(
                totalSeconds,
                settings.text,
                settings.sound,
                settings.color,
                settings.style,
                settings.format,
                usedTimerId,
                settings.hasSound,
                settings.volume,
                settings.pitch
        );

        if ("COUNTDOWN".equals(settings.format)) {
            timer.start();
        }
        TimerManager.getInstance().setTimer(timer);

        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.CREATE, sender));

        return timer;
    }

    public static boolean pauseTimer(@Nullable CommandSender sender) {
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) return false;

        timer.pause();
        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.PAUSE, sender));
        return true;
    }

    public static boolean resumeTimer(@Nullable CommandSender sender) {
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) return false;

        timer.resume();
        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.RESUME, sender));
        return true;
    }

    public static void stopTimer(@Nullable CommandSender sender) {
        TimerManager.getInstance().deleteTimer(sender);
    }

    public static boolean modifyTimer(String action, String value, @Nullable CommandSender sender) {
        MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();

        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null || action == null || action.isEmpty() || value.isEmpty()) return false;

        TimerConfig timerCfg = VoiidCountdownTimer.getConfigsManager().getTimerConfig(timer.getTimerId());
        if (timerCfg == null) {
            if(sender != null) {
                msgManager.send(sender, "timer_status.config_not_found");
            }
            return false;
        }

        switch (action.toLowerCase()) {
            case "add":
                int addSeconds = helper_parseTimeToSeconds(value);
                if (addSeconds <= 0) return false;
                timer.add(addSeconds);
                break;
            case "set":
                int setSeconds = helper_parseTimeToSeconds(value);
                if (setSeconds <= 0) return false;
                timer.set(setSeconds);
                break;
            case "take":
                int takeSeconds = helper_parseTimeToSeconds(value);
                if (takeSeconds <= 0) return false;
                timer.take(takeSeconds);
                break;
            case "bossbar_color":
                try {
                    BarColor color = BarColor.valueOf(value.toUpperCase(java.util.Locale.ROOT));
                    timer.setBossBarColor(color);
                    timerCfg.setColor(color);
                    VoiidCountdownTimer.getConfigsManager().saveTimerConfig(timerCfg);
                    Timer.refreshTimerText();
                } catch (IllegalArgumentException e) {
                    return false;
                }
                break;
            case "bossbar_style":
                try {
                    BarStyle style = BarStyle.valueOf(value.toUpperCase(java.util.Locale.ROOT));
                    timer.setBossBarStyle(style);
                    timerCfg.setStyle(style);
                    VoiidCountdownTimer.getConfigsManager().saveTimerConfig(timerCfg);
                    Timer.refreshTimerText();
                } catch (IllegalArgumentException e) {
                    return false;
                }
                break;
            case "sound":
                timerCfg.setSound(value);
                VoiidCountdownTimer.getConfigsManager().saveTimerConfig(timerCfg);
                Timer.refreshTimerText();
                break;
            case "sound_enable":
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) return false;
                boolean enabled = Boolean.parseBoolean(value);
                timerCfg.setSoundEnabled(enabled);
                VoiidCountdownTimer.getConfigsManager().saveTimerConfig(timerCfg);
                Timer.refreshTimerText();
                break;
            case "sound_volume":
                float vol;
                try {
                    vol = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (vol < 0.1f || vol > 2.0f) return false;
                timerCfg.setSoundVolume(vol);
                VoiidCountdownTimer.getConfigsManager().saveTimerConfig(timerCfg);
                timer.soundVolume = vol;
                break;
            case "sound_pitch":
                float pitch;
                try {
                    pitch = Float.parseFloat(value);
                } catch (NumberFormatException e) {
                    return false;
                }
                if (pitch < 0.1f || pitch > 2.0f) return false;
                timerCfg.setSoundPitch(pitch);
                VoiidCountdownTimer.getConfigsManager().saveTimerConfig(timerCfg);
                timer.soundPitch = pitch;
                break;
            case "text":
                timerCfg.setText(value);
                VoiidCountdownTimer.getConfigsManager().saveTimerConfig(timerCfg);
                Timer.refreshTimerText();
                break;
            default:
                return false;
        }

        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.MODIFY, sender, action.toUpperCase(), value));
        return true;
    }

    public static Timer getTimer() {
        return TimerManager.getInstance().getTimer();
    }

    // Same functions — Without optionals
    public static Timer createTimer(String timeHHMMSS, String timerId) {
        return createTimer(timeHHMMSS, timerId, null);
    }

    public static boolean pauseTimer() {
        return pauseTimer(null);
    }

    public static boolean resumeTimer() {
        return resumeTimer(null);
    }

    public static void stopTimer() {
        stopTimer(null);
    }

    public static boolean modifyTimer(String action, String value) {
        return modifyTimer(action, value, null);
    }

    // Helpers
    public static int helper_parseTimeToSeconds(String hhmmss) {
        if (hhmmss == null || !hhmmss.matches("\\d{1,2}:\\d{2}:\\d{2}")) return -1;
        String[] parts = hhmmss.split(":");
        try {
            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int s = Integer.parseInt(parts[2]);
            if (m > 59 || s > 59) return -1;
            return h * 3600 + m * 60 + s;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
