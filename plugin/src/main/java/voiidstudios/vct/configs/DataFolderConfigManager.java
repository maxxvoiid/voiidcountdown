package voiidstudios.vct.configs;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.configs.model.CustomConfig;

import java.io.File;
import java.util.ArrayList;

public abstract class DataFolderConfigManager {
    protected String folderName;
    protected VoiidCountdownTimer plugin;

    public DataFolderConfigManager(VoiidCountdownTimer plugin, String folderName){
        this.plugin = plugin;
        this.folderName = folderName;
    }

    public void configure() {
        createFolder();
        loadConfigs();
    }

    public void createFolder() {
        File folder = new File(plugin.getDataFolder(), folderName);

        folder.mkdirs();
        createFiles();
    }

    public CustomConfig getConfigFile(String pathName) {
        CustomConfig customConfig = new CustomConfig(pathName, plugin, folderName, true);
        customConfig.registerConfig();
        return customConfig;
    }

    public ArrayList<CustomConfig> getConfigs(){
        ArrayList<CustomConfig> configs = new ArrayList<>();

        String pathFile = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(pathFile);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                String pathName = file.getName();
                CustomConfig commonConfig = new CustomConfig(pathName, plugin, folderName, true);
                commonConfig.registerConfig();
                configs.add(commonConfig);
            }
        }

        return configs;
    }

    public abstract void createFiles();

    public abstract void loadConfigs();

    public abstract void saveConfigs();
}
