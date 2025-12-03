package voiidstudios.vct.utils;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.model.TimerConfig;

public class TimerDefaults {
    public static TimerSettings getSettings(String savedId) {
        String text;
        String sound;
        float volume;
        float pitch;
        BarColor color;
        BarStyle style;
        boolean hasSound;
        String format;
        String usedId = savedId;

        TimerConfig tcfg = VoiidCountdownTimer.getConfigsManager().getTimerConfig(savedId);

        if (tcfg != null && tcfg.isEnabled()) {
            text = tcfg.getText();
            sound = tcfg.getSound();
            volume = tcfg.getSoundVolume();
            pitch = tcfg.getSoundPitch();
            color = tcfg.getColor();
            style = tcfg.getStyle();
            format = TimerConfig.normalizeFormat(tcfg.getFormat());
            hasSound = tcfg.isSoundEnabled();
        } else {
            TimerConfig defaultCfg = VoiidCountdownTimer.getConfigsManager().getTimerConfig("default");
            if (defaultCfg != null && defaultCfg.isEnabled()) {
                if (usedId == null) usedId = "default";
                text = defaultCfg.getText();
                sound = defaultCfg.getSound();
                volume = defaultCfg.getSoundVolume();
                pitch = defaultCfg.getSoundPitch();
                color = defaultCfg.getColor();
                style = defaultCfg.getStyle();
                format = TimerConfig.normalizeFormat(defaultCfg.getFormat());
                hasSound = defaultCfg.isSoundEnabled();
            } else {
                text = "%HH%:%MM%:%SS%";
                sound = "UI_BUTTON_CLICK";
                volume = 1.0f;
                pitch = 1.0f;
                color = BarColor.WHITE;
                style = BarStyle.SOLID;
                format = "COUNTDOWN";
                hasSound = false;
            }
        }

        return new TimerSettings(text, sound, volume, pitch, color, style, format, hasSound, usedId);
    }

    public static class TimerSettings {
        public final String text;
        public final String sound;
        public final float volume;
        public final float pitch;
        public final BarColor color;
        public final BarStyle style;
        public final String format;
        public final boolean hasSound;
        public final String id;

        public TimerSettings(String text, String sound, float volume, float pitch,
                             BarColor color, BarStyle style, String format, boolean hasSound, String id) {
            this.text = text;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.color = color;
            this.style = style;
            this.format = format;
            this.hasSound = hasSound;
            this.id = id;
        }
    }
}
