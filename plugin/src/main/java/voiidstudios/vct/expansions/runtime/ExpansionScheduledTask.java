package voiidstudios.vct.expansions.runtime;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.scheduler.BukkitTask;

import voiidstudios.vct.expansions.ScriptExpansion;

public class ExpansionScheduledTask {
    private final ScriptExpansion expansion;
    private final Object delegate;
    private boolean cancelled = false;

    public ExpansionScheduledTask(ScriptExpansion expansion, Object delegate) {
        this.expansion = expansion;
        this.delegate = delegate;
    }

    public void cancel() {
        if (cancelled) {
            return;
        }

        cancelled = true;

        if (delegate instanceof BukkitTask) {
            ((BukkitTask) delegate).cancel();
        } else if (delegate instanceof ScheduledTask) {
            ((ScheduledTask) delegate).cancel();
        }

        expansion.unregisterTask(this);
    }

    public boolean isCancelled() {
        if (delegate instanceof BukkitTask) {
            return ((BukkitTask) delegate).isCancelled();
        }

        if (delegate instanceof ScheduledTask) {
            return ((ScheduledTask) delegate).isCancelled();
        }

        return cancelled;
    }

    Object getDelegate() {
        return delegate;
    }
}