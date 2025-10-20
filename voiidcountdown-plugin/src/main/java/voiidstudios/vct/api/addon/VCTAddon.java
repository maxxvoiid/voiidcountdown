package voiidstudios.vct.api.addon;

import org.bukkit.plugin.java.JavaPlugin;

import voiidstudios.vct.VoiidCountdownTimer;

/**
 * Contract that external plugins must implement in order to integrate with {@link VoiidCountdownTimer}.
 * <p>
 * A {@code VCTAddon} is registered through the {@link voiidstudios.vct.api.VCTAPI} facade and receives a
 * callback once the main plugin is ready. Implementations should perform any setup inside
 * {@link #onRegister(VoiidCountdownTimer, JavaPlugin)} and release resources within {@link #onUnregister()}.
 */
public interface VCTAddon {

    /**
     * Invoked when the add-on is successfully registered in the main plugin.
     *
     * @param mainPlugin the running instance of {@link VoiidCountdownTimer}
     * @param provider   the {@link JavaPlugin} instance that requested the registration
     */
    void onRegister(VoiidCountdownTimer mainPlugin, JavaPlugin provider);

    /**
     * Invoked when the add-on is unregistered (either explicitly or during plugin shutdown).
     * Implementations should use this moment to cancel tasks and release resources.
     */
    void onUnregister();
}
