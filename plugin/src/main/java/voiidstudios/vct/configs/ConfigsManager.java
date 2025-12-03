package voiidstudios.vct.configs;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.configuration.file.FileConfiguration;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.model.CustomConfig;
import voiidstudios.vct.configs.model.TimerConfig;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigsManager {
    private final MainConfigManager mainConfigManager;
    private final TimersFolderConfigManager timerFolderConfigManager;
    private final CoreFolderConfigManager coreFolderConfigManager;
    private final Map<String, TimerConfig> timersConfigs = new LinkedHashMap<>();

    public ConfigsManager(VoiidCountdownTimer plugin){
        this.mainConfigManager = new MainConfigManager(plugin);
        this.timerFolderConfigManager = new TimersFolderConfigManager(plugin, "timers");
        this.coreFolderConfigManager = new CoreFolderConfigManager(plugin, "core");
    }

    public void configure(){
        coreFolderConfigManager.configure();
        mainConfigManager.configure();
        timerFolderConfigManager.configure();
        configureTimers();
    }

    public boolean reload(){
        try {
            mainConfigManager.reloadConfig();
            configureTimers();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void configureTimers() {
        timersConfigs.clear();

        ArrayList<CustomConfig> vctTimers = getTimersConfigs();

        for(CustomConfig configFile : vctTimers){
            FileConfiguration config = configFile.getConfig();

            if(config.contains("Timers")){
                for (String key : config.getConfigurationSection("Timers").getKeys(false)) {
                    String base = "Timers." + key + ".";

                    if (!config.contains(base + "text")) {
                        config.set(base + "text", "%HH%:%MM%:%SS%");
                    }
                    if (!config.contains(base + "sound")) {
                        config.set(base + "sound", "UI_BUTTON_CLICK");
                    }
                    if (!config.contains(base + "sound_volume")) {
                        config.set(base + "sound_volume", 1.0);
                    }
                    if (!config.contains(base + "sound_pitch")) {
                        config.set(base + "sound_pitch", 1.0);
                    }
                    if (!config.contains(base + "bossbar_color")) {
                        config.set(base + "bossbar_color", "WHITE");
                    }
                    if (!config.contains(base + "bossbar_style")) {
                        config.set(base + "bossbar_style", "SOLID");
                    }
                    if (!config.contains(base + "format")) {
                        config.set(base + "format", "COUNTDOWN");
                    }
                    if (!config.contains(base + "enabled")) {
                        config.set(base + "enabled", true);
                    }
                    if (!config.contains(base + "sound_enabled")) {
                        config.set(base + "sound_enabled", false);
                    }

                    configFile.saveConfig();

                    boolean enabled = config.getBoolean(base + "enabled", true);
                    boolean soundEnabled = config.getBoolean(base + "sound_enabled", false);
                    String text = config.getString(base + "text", "%HH%:%MM%:%SS%");
                    String sound = config.getString(base + "sound", "UI_BUTTON_CLICK");
                    float soundVolume = (float) config.getDouble(base + "sound_volume", 1.0);
                    float soundPitch = (float) config.getDouble(base + "sound_pitch", 1.0);
                    String colorStr = config.getString(base + "bossbar_color", "WHITE");
                    String styleStr = config.getString(base + "bossbar_style", "SOLID");
                    String formatStr = config.getString(base + "format", "COUNTDOWN");

                    BarColor color;
                    try {
                        color = BarColor.valueOf(colorStr.toUpperCase());
                    } catch (Exception e) {
                        color = BarColor.WHITE;
                    }

                    BarStyle style;
                    try {
                        style = BarStyle.valueOf(styleStr.toUpperCase());
                    } catch (Exception e) {
                        style = BarStyle.SOLID;
                    }

                    String format = TimerConfig.normalizeFormat(formatStr);

                    TimerConfig tc = new TimerConfig(key, text, sound, color, style, format, enabled, soundEnabled, soundVolume, soundPitch);

                    timersConfigs.put(key, tc);
                }
            }
        }
    }

    public void saveTimerConfig(TimerConfig tc) {
        for (CustomConfig configFile : getTimersConfigs()) {
            FileConfiguration config = configFile.getConfig();

            String base = "Timers." + tc.getId() + ".";
            if (config.contains(base)) {
                config.set(base + "enabled", tc.isEnabled());
                config.set(base + "sound_enabled", tc.isSoundEnabled());
                config.set(base + "text", tc.getText());
                config.set(base + "sound", tc.getSound());
                config.set(base + "sound_volume", tc.getSoundVolume());
                config.set(base + "sound_pitch", tc.getSoundPitch());
                config.set(base + "bossbar_color", tc.getColor().toString());
                config.set(base + "bossbar_style", tc.getStyle().toString());
                config.set(base + "format", TimerConfig.normalizeFormat(tc.getFormat()));

                configFile.saveConfig();
                break;
            }
        }
    }


    public TimerConfig getTimerConfig(String id) {
        return timersConfigs.get(id);
    }

    public Map<String, TimerConfig> getAllTimerConfigs() {
        return new LinkedHashMap<>(timersConfigs);
    }

    public ArrayList<CustomConfig> getTimersConfigs() {
        ArrayList<CustomConfig> timers = new ArrayList<>();

        timers.add(mainConfigManager.getConfigFile());
        timers.addAll(timerFolderConfigManager.getConfigs());

        return timers;
    }

    public MainConfigManager getMainConfigManager() {
        return mainConfigManager;
    }
}
