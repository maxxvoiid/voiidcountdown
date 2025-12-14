package dev.voiidstudios.ultraapi.updates;

import org.bukkit.plugin.Plugin;

/**
 * Implementations fetch version metadata from remote sources.
 */
public interface UpdateProvider {

    /**
     * @return provider identifier used in logs
     */
    String getName();

    /**
     * Executes a blocking update lookup for the given project.
     *
     * @param plugin    plugin requesting the check
     * @param projectId provider-specific project identifier
     * @return {@link UpdateCheckResult} containing status and remote metadata
     * @throws Exception when the remote call fails or cannot be parsed
     */
    UpdateCheckResult check(Plugin plugin, String projectId) throws Exception;
}
