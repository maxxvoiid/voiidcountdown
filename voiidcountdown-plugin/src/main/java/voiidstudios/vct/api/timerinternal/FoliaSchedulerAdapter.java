package voiidstudios.vct.api.timerinternal;

import org.bukkit.Bukkit;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import voiidstudios.vct.VoiidCountdownTimer;

public class FoliaSchedulerAdapter implements SchedulerAdapter {
    public Object runAtFixedRate(final Runnable runnable, long delay, long period) {
        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(VoiidCountdownTimer.getInstance(), scheduledTask -> runnable.run(), delay, period);
    }

    public void cancel(Object task) {
        ((ScheduledTask) task).cancel();
    }

    public void runDelayed(final Runnable runnable, long delay) {
        Bukkit.getGlobalRegionScheduler().runDelayed(VoiidCountdownTimer.getInstance(), scheduledTask -> runnable.run(), delay);
    }
}
