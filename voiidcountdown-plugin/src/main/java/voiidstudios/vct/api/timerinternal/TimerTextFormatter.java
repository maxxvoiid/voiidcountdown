package voiidstudios.vct.api.timerinternal;

public class TimerTextFormatter {
    public String formatTimerText(String timerText, int seconds) {
        return timerText
                .replace("%HH%", formatTimeHH(seconds))
                .replace("%MM%", formatTimeMM(seconds))
                .replace("%SS%", formatTimeSS(seconds))
                .replace("%H1%", splitDigits(formatTimeHH(seconds))[0])
                .replace("%H2%", splitDigits(formatTimeHH(seconds))[1])
                .replace("%M1%", splitDigits(formatTimeMM(seconds))[0])
                .replace("%M2%", splitDigits(formatTimeMM(seconds))[1])
                .replace("%S1%", splitDigits(formatTimeSS(seconds))[0])
                .replace("%S2%", splitDigits(formatTimeSS(seconds))[1]);
    }

    private String[] splitDigits(String value) {
        if (value == null || value.length() < 2) return new String[]{"0", "0"};
        return new String[]{String.valueOf(value.charAt(0)), String.valueOf(value.charAt(1))};
    }

    public String formatTimeHH(long time) { return String.format("%02d", time / 3600L); }
    public String formatTimeMM(long time) { return String.format("%02d", time % 3600L / 60L); }
    public String formatTimeSS(long time) { return String.format("%02d", time % 60L); }
}
