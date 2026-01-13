package voiidstudios.vct.services;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.update.UpdateChecker;

public class UpdateService {
    private final VoiidCountdownTimer plugin;
    private final UpdateChecker updateChecker;

    public UpdateService(VoiidCountdownTimer plugin) {
        this.plugin = plugin;
        this.updateChecker = new UpdateChecker(plugin.getDescription().getVersion());
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }

    public void checkForUpdates() {
        plugin.checkUpdates(updateChecker.check());
    }
}
