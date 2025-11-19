package voiidstudios.vct.expansions.runtime;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.expansions.ScriptExpansion;
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TranslationManager;
import voiidstudios.vct.utils.ServerCompatibility;

import java.util.Locale;
import java.util.logging.Logger;

public class ScriptExpansionContext {
    private final VoiidCountdownTimer plugin;
    private final ScriptExpansion expansion;
    private final Commands commands;
    private final Scheduler scheduler;

    public ScriptExpansionContext(VoiidCountdownTimer plugin, ScriptExpansion expansion) {
        this.plugin = plugin;
        this.expansion = expansion;
        this.commands = new Commands();
        this.scheduler = new Scheduler();
    }

    public VoiidCountdownTimer getPlugin() {
        return plugin;
    }

    public Logger getLogger() {
        return plugin.getLogger();
    }

    public MessagesManager getMessages() {
        return VoiidCountdownTimer.getMessagesManager();
    }

    public TranslationManager.TranslationBundle getTranslations(String namespace) {
        return VoiidCountdownTimer.getMessagesManager().getCustomTranslations(namespace);
    }

    public Commands getCommands() {
        return commands;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public ExpansionScheduledTask runTask(ScriptObjectMirror function) {
        return scheduler.runTask(function);
    }

    public ExpansionScheduledTask runTaskLater(ScriptObjectMirror function, long delay) {
        return scheduler.runTaskLater(function, delay);
    }

    public ExpansionScheduledTask runTaskTimer(ScriptObjectMirror function, long delay, long period) {
        return scheduler.runTaskTimer(function, delay, period);
    }

    public class Commands {
        public void register(Object definition) {
            expansion.registerCommand(definition);
        }
    }

    public class Scheduler {
        public ExpansionScheduledTask runTask(Object function) {
            return schedule(function, 0L, 0L, SchedulerType.RUN);
        }

        public ExpansionScheduledTask runTaskLater(Object function, long delay) {
            return schedule(function, delay, 0L, SchedulerType.DELAYED);
        }

        public ExpansionScheduledTask runTaskTimer(Object function, long delay, long period) {
            return schedule(function, delay, period, SchedulerType.REPEATING);
        }

        private ExpansionScheduledTask schedule(Object function, long delay, long period, SchedulerType type) {
            ScriptObjectMirror mirror = expansion.requireFunction(function);
            Runnable runnable = expansion.toRunnable(mirror);

            if (ServerCompatibility.isFolia()) {
                switch (type) {
                    case RUN:
                        ScheduledTask runTask = Bukkit.getGlobalRegionScheduler().run(plugin, scheduledTask -> runnable.run());
                        return expansion.trackTask(runTask);
                    case DELAYED:
                        ScheduledTask delayed = Bukkit.getGlobalRegionScheduler().runDelayed(plugin, scheduledTask -> runnable.run(), delay);
                        return expansion.trackTask(delayed);
                    case REPEATING:
                        ScheduledTask repeating = Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, scheduledTask -> runnable.run(), Math.max(1L, delay), Math.max(1L, period));
                        return expansion.trackTask(repeating);
                }
            } else {
                switch (type) {
                    case RUN:
                        BukkitTask runTask = Bukkit.getScheduler().runTask(plugin, runnable);
                        return expansion.trackTask(runTask);
                    case DELAYED:
                        BukkitTask delayed = Bukkit.getScheduler().runTaskLater(plugin, runnable, Math.max(1L, delay));
                        return expansion.trackTask(delayed);
                    case REPEATING:
                        BukkitTask repeating = Bukkit.getScheduler().runTaskTimer(plugin, runnable, Math.max(1L, delay), Math.max(1L, period));
                        return expansion.trackTask(repeating);
                }
            }

            throw new IllegalStateException(String.format(Locale.ROOT, "Unsupported scheduler type %s", type));
        }
    }

    private enum SchedulerType {
        RUN,
        DELAYED,
        REPEATING
    }
}