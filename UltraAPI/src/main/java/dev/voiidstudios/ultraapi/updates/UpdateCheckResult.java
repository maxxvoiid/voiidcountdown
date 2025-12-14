package dev.voiidstudios.ultraapi.updates;

import org.bukkit.plugin.Plugin;

/**
 * Represents the result of a remote update check.
 */
public class UpdateCheckResult {

    private final String provider;
    private final String currentVersion;
    private final String latestVersion;
    private final String downloadUrl;
    private final boolean error;
    private final boolean disabled;

    private UpdateCheckResult(String provider, String currentVersion, String latestVersion, String downloadUrl, boolean error, boolean disabled) {
        this.provider = provider;
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
        this.downloadUrl = downloadUrl;
        this.error = error;
        this.disabled = disabled;
    }

    public static UpdateCheckResult disabled(Plugin plugin) {
        return new UpdateCheckResult(null, plugin.getDescription().getVersion(), null, null, false, true);
    }

    public static UpdateCheckResult error(Plugin plugin, String provider) {
        return new UpdateCheckResult(provider, plugin.getDescription().getVersion(), null, null, true, false);
    }

    public static UpdateCheckResult noUpdate(String provider, Plugin plugin) {
        return new UpdateCheckResult(provider, plugin.getDescription().getVersion(), plugin.getDescription().getVersion(), null, false, false);
    }

    public static UpdateCheckResult updateAvailable(String provider, Plugin plugin, String latestVersion, String downloadUrl) {
        return new UpdateCheckResult(provider, plugin.getDescription().getVersion(), latestVersion, downloadUrl, false, false);
    }

    public String getProvider() {
        return provider;
    }

    public String getCurrentVersion() {
        return currentVersion;
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public boolean isError() {
        return error;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public boolean isUpdateAvailable() {
        return latestVersion != null && !latestVersion.equalsIgnoreCase(currentVersion);
    }
}
