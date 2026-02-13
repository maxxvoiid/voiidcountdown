package voiidstudios.vct.api.timerinternal;

public interface SchedulerAdapter {
    Object runAtFixedRate(Runnable runnable, long delay, long period);

    void cancel(Object task);

    void runDelayed(Runnable runnable, long delay);
}
