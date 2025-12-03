package voiidstudios.vct.configs.model;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import java.util.Locale;

public class TimerConfig {
    private String id;
    private String text;
    private String sound;
    private float soundVolume;
    private float soundPitch;
    private BarColor color;
    private BarStyle style;
    private String format;
    private boolean enabled;
    private boolean soundEnabled;

    public TimerConfig(String id, String text, String sound, BarColor color, BarStyle style, String format, boolean enabled, boolean soundEnabled, float soundVolume, float soundPitch) {
        this.id = id;
        this.text = text;
        this.sound = sound;
        this.soundVolume = soundVolume;
        this.soundPitch = soundPitch;
        this.color = color;
        this.style = style;
        this.format = normalizeFormat(format);
        this.enabled = enabled;
        this.soundEnabled = soundEnabled;
    }

    public String getId() { return this.id; }
    public String getText() { return this.text; }
    public String getSound() { return this.sound; }
    public float getSoundVolume() { return this.soundVolume; }
    public float getSoundPitch() { return this.soundPitch; }
    public BarColor getColor() { return this.color; }
    public BarStyle getStyle() { return this.style; }
    public String getFormat() { return this.format; }
    public String getFormatName() { return this.format; }
    public boolean isEnabled() { return this.enabled; }
    public boolean isSoundEnabled() { return this.soundEnabled; }

    public void setText(String text) { this.text = text; }
    public void setSound(String sound) { this.sound = sound; }
    public void setSoundVolume(float volume) { this.soundVolume = volume; }
    public void setSoundPitch(float pitch) { this.soundPitch = pitch; }
    public void setColor(BarColor color) { this.color = color; }
    public void setStyle(BarStyle style) { this.style = style; }
    public void setFormat(String format) { this.format = normalizeFormat(format); }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setSoundEnabled(boolean enabled) { this.soundEnabled = enabled; }

    public static String normalizeFormat(String format) {
        return format == null ? "COUNTDOWN" : format.toUpperCase(Locale.ROOT);
    }
}