package voiidstudios.vct.configs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.bukkit.configuration.file.FileConfiguration;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.model.CustomConfig;
import voiidstudios.vct.utils.Formatter;

public class MainConfigManager {
    private CustomConfig configFile;

    private String language;
    private boolean auto_update;
    private boolean update_notification;
    private int ticks_hide_after_ending;
    private String text_format;
    private int refresh_ticks;
    private boolean save_state_timers;
    private boolean debug_mode;

    public MainConfigManager(VoiidCountdownTimer plugin){
        configFile = new CustomConfig("config.yml", plugin, null, false);
        configFile.registerConfig();
        checkConfigsUpdate();
    }

    public void configure(){
        FileConfiguration config = configFile.getConfig();

        language = config.getString("Config.language", "en_US");
        auto_update = config.getBoolean("Config.auto_update");
        update_notification = config.getBoolean("Config.update_notification");
        ticks_hide_after_ending = config.getInt("Config.ticks_hide_after_ending");
        text_format = config.getString("Config.text_format");
        refresh_ticks = config.getInt("Config.refresh_ticks");
        save_state_timers = config.getBoolean("Config.save_state_timers");
        debug_mode = config.getBoolean("Config.debug_mode");
    }

    public void reloadConfig(){
        configFile.reloadConfig();
        configure();
    }

    public FileConfiguration getConfig(){
        return configFile.getConfig();
    }

    public CustomConfig getConfigFile(){
        return this.configFile;
    }

    public void saveConfig(){
        configFile.saveConfig();
    }

    public void checkConfigsUpdate(){
        Path pathConfig = Paths.get(configFile.getRoute());
        try {
            String text = new String(Files.readAllBytes(pathConfig));
            
            if(!text.contains("language:")){
                getConfig().set("Config.language", "en_US");
                saveConfig();
            }
            if(!text.contains("auto_update:")){
                getConfig().set("Config.auto_update", true);
                saveConfig();
            }
            if(!text.contains("update_notification:")){
                getConfig().set("Config.update_notification", true);
                saveConfig();
            }
            if(!text.contains("ticks_hide_after_ending:")){
                getConfig().set("Config.ticks_hide_after_ending", 60);
                saveConfig();
            }
            if(!text.contains("text_format:")){
                getConfig().set("Config.text_format", "LEGACY");
                saveConfig();
            }
            if(!text.contains("refresh_ticks:")){
                getConfig().set("Config.refresh_ticks", 10);
                saveConfig();
            }
            if(!text.contains("save_state_timers:")){
                getConfig().set("Config.save_state_timers", true);
                saveConfig();
            }
            if(!text.contains("debug_mode:")){
                getConfig().set("Config.debug_mode", false);
                saveConfig();
            }

            if (getConfig().contains("Timers")) {
                for (String timerKey : getConfig().getConfigurationSection("Timers").getKeys(false)) {
                    String base = "Timers." + timerKey + ".";

                    if (!getConfig().contains(base + "text")) {
                        getConfig().set(base + "text", "%HH%:%MM%:%SS%");
                    }
                    if (!getConfig().contains(base + "sound")) {
                        getConfig().set(base + "sound", "UI_BUTTON_CLICK");
                    }
                    if (!getConfig().contains(base + "sound_volume")) {
                        getConfig().set(base + "sound_volume", 1.0);
                    }
                    if (!getConfig().contains(base + "sound_pitch")) {
                        getConfig().set(base + "sound_pitch", 1.0);
                    }
                    if (!getConfig().contains(base + "bossbar_color")) {
                        getConfig().set(base + "bossbar_color", "WHITE");
                    }
                    if (!getConfig().contains(base + "bossbar_style")) {
                        getConfig().set(base + "bossbar_style", "SOLID");
                    }
                    if (!getConfig().contains(base + "enabled")) {
                        getConfig().set(base + "enabled", true);
                    }
                    if (!getConfig().contains(base + "sound_enabled")) {
                        getConfig().set(base + "sound_enabled", false);
                    }
                }
                saveConfig();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public String getLanguage() {
        return language != null ? language : "en_US";
    }

    public boolean isAuto_update() {
        return auto_update;
    }

    public boolean isUpdate_notification() {
        return update_notification;
    }

    public boolean isSave_state_timers() {
        return save_state_timers;
    }

    public int getTicks_hide_after_ending() {
        return ticks_hide_after_ending;
    }

    public Formatter getFormatter() {
        try {
            return Formatter.valueOf(text_format.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Formatter.LEGACY;
        }
    }

    public int getRefresh_ticks() {
        return refresh_ticks;
    }

    public boolean isDebug_mode() {
        return debug_mode;
    }
}
