package voiidstudios.vct.configs;

import voiidstudios.vct.VoiidCountdownTimer;

import java.io.File;
import java.io.InputStream;
import java.io.FileOutputStream;

public class CoreFolderConfigManager extends DataFolderConfigManager {
    public CoreFolderConfigManager(VoiidCountdownTimer plugin, String folderName) {
        super(plugin, folderName);
    }

    @Override
    public void createFiles() {
        File originsFolder = new File(plugin.getDataFolder(), "core/messages/origins");
        File customFolder  = new File(plugin.getDataFolder(), "core/messages/custom");

        originsFolder.mkdirs();
        customFolder.mkdirs();

        copyResourceIfMissing("core/messages/origins/en_US.yml");
        copyResourceIfMissing("core/messages/custom/custom.yml");
    }

    private void copyResourceIfMissing(String path) {
        File dest = new File(plugin.getDataFolder(), path);
        if (dest.exists()) return;

        if (dest.getParentFile() != null) {
            dest.getParentFile().mkdirs();
        }

        try (InputStream in = plugin.getResource(path)) {

            if (in == null) {
                plugin.getLogger().warning("Missing resource in JAR: " + path);
                return;
            }

            try (FileOutputStream out = new FileOutputStream(dest)) {
                in.transferTo(out);
            }

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to copy default file: " + path);
            e.printStackTrace();
        }
    }

    @Override
    public void loadConfigs() {}

    @Override
    public void saveConfigs() {}
}