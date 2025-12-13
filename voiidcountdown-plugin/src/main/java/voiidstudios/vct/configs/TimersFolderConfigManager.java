package voiidstudios.vct.configs;

import dev.voiidstudios.ultraapi.UltraAPI;
import voiidstudios.vct.VoiidCountdownTimer;

public class TimersFolderConfigManager extends DataFolderConfigManager {
    public TimersFolderConfigManager(VoiidCountdownTimer plugin, String folderName) {
        super(plugin, folderName);
    }

    @Override
    public void createFiles() {
        UltraAPI.config(plugin, folderName + "/more_timers.yml");
    }

    @Override
    public void loadConfigs() {

    }

    @Override
    public void saveConfigs() {

    }
}
