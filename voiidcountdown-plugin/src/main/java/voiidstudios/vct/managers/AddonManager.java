package voiidstudios.vct.managers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.addon.VCTAddon;
import voiidstudios.vct.api.addon.VCTAddonEvent;

/**
 * Keeps track of registered {@link VCTAddon} instances and orchestrates their lifecycle callbacks.
 */
public class AddonManager {

    private final Map<String, RegisteredAddon> addons = new HashMap<>();
    private final VoiidCountdownTimer mainPlugin;
    private final Logger logger;

    public AddonManager(VoiidCountdownTimer mainPlugin) {
        this.mainPlugin = mainPlugin;
        this.logger = mainPlugin.getLogger();
    }

    public Map<String, RegisteredAddon> getAddons() {
        return Collections.unmodifiableMap(addons);
    }

    public boolean registerAddon(String id, VCTAddon addon, JavaPlugin provider) {
        if (id == null || id.trim().isEmpty() || addon == null || provider == null) {
            return false;
        }

        String normalizedId = id.trim().toLowerCase(java.util.Locale.ROOT);
        if (addons.containsKey(normalizedId)) {
            logger.warning("Attempted to register duplicated VCT add-on with id '" + normalizedId + "'");
            return false;
        }

        try {
            addon.onRegister(mainPlugin, provider);
        } catch (Throwable t) {
            logger.severe("Could not initialise VCT add-on '" + normalizedId + "' provided by " + provider.getName());
            t.printStackTrace();
            return false;
        }

        addons.put(normalizedId, new RegisteredAddon(addon, provider));
        logger.info("Registered VCT add-on '" + normalizedId + "' from " + provider.getName());

        Bukkit.getPluginManager().callEvent(new VCTAddonEvent(normalizedId, addon, provider, mainPlugin, VCTAddonEvent.Type.REGISTER));
        return true;
    }

    public boolean unregisterAddon(String id) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }

        String normalizedId = id.trim().toLowerCase(java.util.Locale.ROOT);
        RegisteredAddon registered = addons.remove(normalizedId);
        if (registered == null) {
            return false;
        }

        try {
            registered.addon.onUnregister();
        } catch (Throwable t) {
            logger.warning("An error occurred while disabling VCT add-on '" + normalizedId + "': " + t.getMessage());
        }

        logger.info("Unregistered VCT add-on '" + normalizedId + "'");
        Bukkit.getPluginManager().callEvent(new VCTAddonEvent(normalizedId, registered.addon, registered.provider, mainPlugin, VCTAddonEvent.Type.UNREGISTER));
        return true;
    }

    public void unregisterAll() {
        for (String id : addons.keySet().toArray(new String[0])) {
            unregisterAddon(id);
        }
        addons.clear();
    }

    public static class RegisteredAddon {
        private final VCTAddon addon;
        private final JavaPlugin provider;

        public RegisteredAddon(VCTAddon addon, JavaPlugin provider) {
            this.addon = addon;
            this.provider = provider;
        }

        public VCTAddon getAddon() {
            return addon;
        }

        public JavaPlugin getProvider() {
            return provider;
        }
    }
}
