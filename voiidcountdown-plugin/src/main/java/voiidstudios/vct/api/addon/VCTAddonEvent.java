package voiidstudios.vct.api.addon;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import voiidstudios.vct.VoiidCountdownTimer;

/**
 * Bukkit event fired whenever an add-on registers or unregisters from the main plugin.
 */
public class VCTAddonEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    public enum Type {
        REGISTER,
        UNREGISTER
    }

    private final String addonId;
    private final VCTAddon addon;
    private final JavaPlugin provider;
    private final VoiidCountdownTimer mainPlugin;
    private final Type type;

    public VCTAddonEvent(String addonId, VCTAddon addon, JavaPlugin provider, VoiidCountdownTimer mainPlugin, Type type) {
        this.addonId = addonId;
        this.addon = addon;
        this.provider = provider;
        this.mainPlugin = mainPlugin;
        this.type = type;
    }

    public String getAddonId() {
        return addonId;
    }

    public VCTAddon getAddon() {
        return addon;
    }

    public JavaPlugin getProvider() {
        return provider;
    }

    public VoiidCountdownTimer getMainPlugin() {
        return mainPlugin;
    }

    public Type getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
