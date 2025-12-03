package voiidstudios.vct.managers;

import org.bukkit.Bukkit;
import voiidstudios.vct.VoiidCountdownTimer;

public class DependencyManager {
    private boolean placeholderAPI;
    private boolean paper;

    public DependencyManager(VoiidCountdownTimer plugin){
        placeholderAPI = false;
        paper = false;

        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
                && Bukkit.getPluginManager().getPlugin("PlaceholderAPI").isEnabled()){
            placeholderAPI = true;
        }

        try{
            Class.forName("com.destroystokyo.paper.ParticleBuilder");
            paper = true;
        }catch(Exception e){}
    }

    public boolean isPlaceholderAPI() {
        return placeholderAPI;
    }

    public boolean isPaper() {
        return paper;
    }
}
