package voiidstudios.vct.api;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.model.TimerConfig;
import voiidstudios.vct.managers.TimerManager;

public class PAPIExpansion extends PlaceholderExpansion {
    private final JavaPlugin plugin;

    public PAPIExpansion(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    // %vct_timer_hhmmss:<timer_id>% - Get the hours, minutes and seconds (00:00:00) of the timer
    // %vct_timer_hh:<timer_id>% - Get the hours (00) of the timer
    // %vct_timer_h1:<timer_id>% - Get the first digit of the timer hours (0)
    // %vct_timer_h2:<timer_id>% - Get the second digit of the timer hours (0)
    // %vct_timer_mm:<timer_id>% - Get the minutes (00) of the timer
    // %vct_timer_m1:<timer_id>% - Get the first digit of the timer minutes (0)
    // %vct_timer_m2:<timer_id>% - Get the second digit of the timer minutes (0)
    // %vct_timer_ss:<timer_id>% - Get the seconds (00) of the timer
    // %vct_timer_s1:<timer_id>% - Get the first digit of the timer seconds (0)
    // %vct_timer_s2:<timer_id>% - Get the second digit of the timer seconds (0)

    // %vct_timer_active:<timer_id>% - Return 'true' or 'false' if the timer is active (THERE IS A TIMER).
    // %vct_timer_running:<timer_id>% - Return 'true' or 'false' if the timer is running (NOT PAUSED).
    // %vct_timer_paused:<timer_id>% - Return 'true' or 'false' if the timer is paused.

    @Override
    public @NotNull String getIdentifier() {
        return "vct";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(",", plugin.getDescription().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        String id = null;
        String base = params;
        int idx = params.indexOf(':'); 
        if (idx > 0) {
            base = params.substring(0, idx); // timer_ss
            id = params.substring(idx + 1); // timer_3
            if (id.isEmpty()) id = null;
        }

        switch (base.toLowerCase()) {
            case "timer_hhmmss":
                return getTimerValueHHMMSS(id);
            case "timer_hh":
                return getTimerValueHH(id);
            case "timer_h1":
                return getTimerValueH1(id);
            case "timer_h2":
                return getTimerValueH2(id);
            case "timer_mm":
                return getTimerValueMM(id);
            case "timer_m1":
                return getTimerValueM1(id);
            case "timer_m2":
                return getTimerValueM2(id);
            case "timer_ss":
                return getTimerValueSS(id);
            case "timer_s1":
                return getTimerValueS1(id);
            case "timer_s2":
                return getTimerValueS2(id);
            case "timer_active":
                return isTimerActive(id);
            case "timer_running":
                return isTimerRunning(id);
            case "timer_paused":
                return isTimerPaused(id);
            default:
                return null;
        }
    }

    private Timer getActiveTimerIfMatches(String id) {
        Timer current = TimerManager.getInstance().getTimer();
        if (id == null) return current;
        if (current != null && current.getTimerId() != null && current.getTimerId().equalsIgnoreCase(id)) {
            return current;
        }
        return null;
    }

    private TimerConfig getConfigIfExists(String id) {
        if (id == null) return null;
        return VoiidCountdownTimer.getConfigsManager().getTimerConfig(id);
    }

    private String getTimerValueHHMMSS(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeft();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "00:00:00" : null;
    }

    private String getTimerValueHH(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftHH();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "00" : null;
    }

    private String getTimerValueH1(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftHHDigit1();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "0" : null;
    }

    private String getTimerValueH2(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftHHDigit2();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "0" : null;
    }

    private String getTimerValueMM(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftMM();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "00" : null;
    }

    private String getTimerValueM1(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftMMDigit1();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "0" : null;
    }

    private String getTimerValueM2(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftMMDigit2();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "0" : null;
    }

    private String getTimerValueSS(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftSS();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "00" : null;
    }

    private String getTimerValueS1(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftSSDigit1();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "0" : null;
    }

    private String getTimerValueS2(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return t.getTimeLeftSSDigit2();

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "0" : null;
    }

    private String isTimerActive(String id) {
        if (id == null) {
            return String.valueOf(TimerManager.getInstance().getTimer() != null);
        }

        Timer t = getActiveTimerIfMatches(id);
        if (t != null) return "true";

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "false" : null;
    }

    private String isTimerRunning(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) {
            return String.valueOf(t.isActive());
        }

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "false" : null;
    }

    private String isTimerPaused(String id) {
        Timer t = getActiveTimerIfMatches(id);
        if (t != null) {
            return String.valueOf(!t.isActive());
        }

        TimerConfig cfg = getConfigIfExists(id);
        return cfg != null ? "false" : null;
    }
}
