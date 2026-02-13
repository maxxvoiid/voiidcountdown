package voiidstudios.vct.api.timerinternal;

import voiidstudios.vct.utils.ServerCompatibility;

public final class SchedulerAdapters {
    private SchedulerAdapters() {}

    public static SchedulerAdapter createDefault() {
        if (ServerCompatibility.isFolia()) {
            return new FoliaSchedulerAdapter();
        }
        return new BukkitSchedulerAdapter();
    }
}
