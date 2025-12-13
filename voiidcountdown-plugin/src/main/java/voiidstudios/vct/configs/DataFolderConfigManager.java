package voiidstudios.vct.configs;

import dev.voiidstudios.ultraapi.UltraAPI;
import dev.voiidstudios.ultraapi.config.UConfig;
import voiidstudios.vct.VoiidCountdownTimer;

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

    public void createFolder(){
        File folder;
        try {
            folder = new File(plugin.getDataFolder() + File.separator + folderName);
            if(!folder.exists()){
                folder.mkdirs();
                createFiles();
            }
        } catch(SecurityException e) {
            folder = null;
        }
    }

    public UConfig getConfigFile(String pathName) {
        return UltraAPI.config(plugin, folderName + File.separator + pathName);
    }

    public ArrayList<UConfig> getConfigs(){
        ArrayList<UConfig> configs = new ArrayList<>();

        String pathFile = plugin.getDataFolder() + File.separator + folderName;
        File folder = new File(pathFile);
        File[] listOfFiles = folder.listFiles();
        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    String pathName = file.getName();
                    UConfig commonConfig = UltraAPI.config(plugin, folderName + File.separator + pathName);
                    configs.add(commonConfig);
                }
            }
        }

        return configs;
    }

    public abstract void createFiles();

    public abstract void loadConfigs();

    public abstract void saveConfigs();
}
