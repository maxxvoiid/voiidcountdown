package voiidstudios.vct.addons.stopwatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import voiidstudios.vct.api.Timer;

public class StopwatchCommand implements CommandExecutor, TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList("start", "stop", "pause", "resume");
    private final StopwatchAddon addon;

    public StopwatchCommand(StopwatchAddon addon) {
        this.addon = addon;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!addon.isReady()) {
            sender.sendMessage("§cVoiidCountdownTimer is not available right now.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase(java.util.Locale.ROOT);
        switch (sub) {
            case "start":
                handleStart(sender, label, args);
                break;
            case "stop":
                addon.stopTimer(sender);
                sender.sendMessage("§aStopwatch stopped.");
                break;
            case "pause":
                if (addon.pauseTimer(sender)) {
                    sender.sendMessage("§eStopwatch paused.");
                } else {
                    sender.sendMessage("§cNo active stopwatch to pause.");
                }
                break;
            case "resume":
                if (addon.resumeTimer(sender)) {
                    sender.sendMessage("§aStopwatch resumed.");
                } else {
                    sender.sendMessage("§cNo paused stopwatch to resume.");
                }
                break;
            default:
                sendUsage(sender, label);
        }
        return true;
    }

    private void handleStart(CommandSender sender, String label, String[] args) {
        String target = null;
        String timerId = StopwatchAddonPlugin.ADDON_ID;

        if (args.length >= 2) {
            if (args[1].contains(":")) {
                target = args[1];
            } else if (!args[1].equalsIgnoreCase("default") && !args[1].equalsIgnoreCase("none")) {
                timerId = args[1];
            }
        }
        if (args.length >= 3) {
            if (args[2].contains(":") && target == null) {
                target = args[2];
            } else {
                timerId = args[2];
            }
        }

        Timer created;
        if (target != null) {
            created = addon.startStopwatch(timerId, target, sender);
        } else {
            created = addon.startStopwatch(timerId, (Integer) null, sender);
        }

        if (created == null) {
            sender.sendMessage("§cUnable to create stopwatch. Check the syntax or if a timer is already running.");
            sendUsage(sender, label);
        } else {
            sender.sendMessage("§aStopwatch started in mode §f" + created.getMode() + "§a.");
        }
    }

    private void sendUsage(CommandSender sender, String label) {
        sender.sendMessage("§dUsage:");
        sender.sendMessage("§7/" + label + " start [hh:mm:ss] [timer-id]");
        sender.sendMessage("§7/" + label + " pause");
        sender.sendMessage("§7/" + label + " resume");
        sender.sendMessage("§7/" + label + " stop");
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], SUBCOMMANDS, new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("start")) {
            return Collections.singletonList("00:05:00");
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("start")) {
            return Collections.singletonList(StopwatchAddonPlugin.ADDON_ID);
        }

        return Collections.emptyList();
    }
}
