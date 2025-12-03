package voiidstudios.vct.configs;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.model.CustomConfig;

public class TimersFolderConfigManager extends DataFolderConfigManager {
    public TimersFolderConfigManager(VoiidCountdownTimer plugin, String folderName) {
        super(plugin, folderName);
    }

    @Override
    public void createFiles() {
        new CustomConfig("more_timers.yml",plugin,folderName,false).registerConfig();
    }

    @Override
    public void loadConfigs() {

    }

    @Override
    public void saveConfigs() {

    }
}
