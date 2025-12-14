package dev.voiidstudios.ultraapi.updates;

import dev.voiidstudios.ultraapi.config.UConfig;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Centralized asynchronous update checker exposed through UltraAPI.
 */
public class UpdateService {

    private final Map<String, UpdateProvider> providers = new HashMap<>();
    private final Logger logger;
    private final UConfig settings;

    public UpdateService(Logger logger, UConfig settings) {
        this.logger = logger;
        this.settings = settings;
        registerProvider(new ModrinthUpdateProvider());
        registerProvider(new GithubReleaseUpdateProvider());
    }

    private void registerProvider(UpdateProvider provider) {
        providers.put(provider.getName().toLowerCase(), provider);
    }

    /**
     * Executes an asynchronous update check.
     *
     * @param plugin    plugin requesting the check
     * @param provider  update source ("modrinth" or "github")
     * @param projectId provider-specific project identifier
     * @return future that completes with the {@link UpdateCheckResult}
     */
    public CompletableFuture<UpdateCheckResult> check(Plugin plugin, String provider, String projectId) {
        UpdateProvider updateProvider = providers.get(provider.toLowerCase());
        boolean updatesEnabled = settings.getBoolean("Updates.enabled", true);

        if (updateProvider == null) {
            logger.warning("Unknown update provider '" + provider + "'.");
            return CompletableFuture.completedFuture(UpdateCheckResult.error(plugin, provider));
        }

        if (!updatesEnabled) {
            logger.info("Update checks are disabled in UltraAPI/config.yml");
            return CompletableFuture.completedFuture(UpdateCheckResult.disabled(plugin));
        }

        CompletableFuture<UpdateCheckResult> future = new CompletableFuture<>();
        Runnable task = () -> {
            try {
                UpdateCheckResult result = updateProvider.check(plugin, projectId);
                logResult(plugin, result);
                future.complete(result);
            } catch (Exception e) {
                logger.severe("Error while checking updates for " + plugin.getName() + ": " + e.getMessage());
                future.complete(UpdateCheckResult.error(plugin, provider));
            }
        };

        if (Bukkit.isPrimaryThread()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        } else {
            task.run();
        }

        return future;
    }

    private void logResult(Plugin plugin, UpdateCheckResult result) {
        if (result.isDisabled()) {
            return;
        }
        if (result.isError()) {
            logger.warning("[" + plugin.getName() + "] Error checking updates using " + result.getProvider());
            return;
        }
        if (result.isUpdateAvailable()) {
            logger.info("[" + plugin.getName() + "] New version available: " + result.getLatestVersion());
        } else {
            logger.info("[" + plugin.getName() + "] Plugin is up to date.");
        }
    }
}
