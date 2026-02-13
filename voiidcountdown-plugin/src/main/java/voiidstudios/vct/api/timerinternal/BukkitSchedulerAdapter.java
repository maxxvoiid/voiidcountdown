package voiidstudios.vct.api.timerinternal;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import voiidstudios.vct.VoiidCountdownTimer;

public class BukkitSchedulerAdapter implements SchedulerAdapter {
    public Object runAtFixedRate(Runnable runnable, long delay, long period) {
        return Bukkit.getScheduler().runTaskTimer(VoiidCountdownTimer.getInstance(), runnable, delay, period);
    }

    public void cancel(Object task) {
        ((BukkitTask) task).cancel();
    }

    public void runDelayed(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(VoiidCountdownTimer.getInstance(), runnable, delay);
    }
}
