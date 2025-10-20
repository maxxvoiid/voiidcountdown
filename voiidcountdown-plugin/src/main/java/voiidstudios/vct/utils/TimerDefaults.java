package voiidstudios.vct.utils;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.TimerMode;
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
        String usedId = savedId;

        TimerConfig tcfg = VoiidCountdownTimer.getConfigsManager().getTimerConfig(savedId);

        if (tcfg != null && tcfg.isEnabled()) {
            text = tcfg.getText();
            sound = tcfg.getSound();
            volume = tcfg.getSoundVolume();
            pitch = tcfg.getSoundPitch();
            color = tcfg.getColor();
            style = tcfg.getStyle();
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
                hasSound = defaultCfg.isSoundEnabled();
            } else {
                text = "%HH%:%MM%:%SS%";
                sound = "UI_BUTTON_CLICK";
                volume = 1.0f;
                pitch = 1.0f;
                color = BarColor.WHITE;
                style = BarStyle.SOLID;
                hasSound = false;
            }
        }

        return new TimerSettings(text, sound, volume, pitch, color, style, hasSound, usedId, TimerMode.COUNTDOWN, null);
    }

    public static class TimerSettings {
        public final String text;
        public final String sound;
        public final float volume;
        public final float pitch;
        public final BarColor color;
        public final BarStyle style;
        public final boolean hasSound;
        public final String id;
        public final TimerMode mode;
        public final Integer targetSeconds;

        public TimerSettings(String text, String sound, float volume, float pitch,
                             BarColor color, BarStyle style, boolean hasSound, String id,
                             TimerMode mode, Integer targetSeconds) {
            this.text = text;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
            this.color = color;
            this.style = style;
            this.hasSound = hasSound;
            this.id = id;
            this.mode = mode == null ? TimerMode.COUNTDOWN : mode;
            this.targetSeconds = targetSeconds;
        }
    }
}
