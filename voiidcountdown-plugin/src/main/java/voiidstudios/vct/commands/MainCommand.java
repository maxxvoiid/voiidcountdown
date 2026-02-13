package voiidstudios.vct.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.commands.subcommands.ExpansionSubcommand;
import voiidstudios.vct.commands.subcommands.HelpSubcommand;
import voiidstudios.vct.commands.subcommands.ModifySubcommand;
import voiidstudios.vct.commands.subcommands.PauseSubcommand;
import voiidstudios.vct.commands.subcommands.ReloadSubcommand;
import voiidstudios.vct.commands.subcommands.ResumeSubcommand;
import voiidstudios.vct.commands.subcommands.SetSubcommand;
import voiidstudios.vct.commands.subcommands.StopSubcommand;
import voiidstudios.vct.commands.subcommands.SubcommandContext;
import voiidstudios.vct.configs.model.TimerConfig;
import voiidstudios.vct.expansions.ExpansionManager;
import voiidstudios.vct.managers.MessagesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class MainCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN_PERMISSION = "voiidcountdowntimer.admin";

    private final VCTCommandRouter router;

    public MainCommand() {
        SubcommandContext context = new SubcommandContext(this);
        this.router = new VCTCommandRouter(this);
        this.router.register(new HelpSubcommand(context));
        this.router.register(new ReloadSubcommand(context));
        this.router.register(new SetSubcommand(context));
        this.router.register(new PauseSubcommand(context));
        this.router.register(new ResumeSubcommand(context));
        this.router.register(new StopSubcommand());
        this.router.register(new ModifySubcommand(context));
        this.router.register(new ExpansionSubcommand(context));
    }

    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (router.dispatch(sender, args)) {
            return true;
        }

        ExpansionManager expansionManager = getExpansionManager();
        if (expansionManager != null && expansionManager.executeCommand(sender, args)) {
            return true;
        }

        if (!hasPermission(sender, "voiidcountdowntimer.command.help")) {
            getMessagesManager().send(sender, "command.no_permissions");
            return true;
        }

        help(sender);
        return true;
    }

    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        ExpansionManager expansionManager = getExpansionManager();

        if (args.length == 1) {
            LinkedHashSet<String> completions = new LinkedHashSet<String>();
            List<String> routerRoot = router.tabComplete(sender, args);
            if (routerRoot != null) {
                completions.addAll(routerRoot);
            }

            if (expansionManager != null) {
                completions.addAll(expansionManager.getRootSuggestions(args[0]));
            }

            return completions.isEmpty() ? null : new ArrayList<String>(completions);
        }

        List<String> subCompletions = router.tabComplete(sender, args);
        if (subCompletions != null && !subCompletions.isEmpty()) {
            return subCompletions;
        }

        if (expansionManager != null) {
            List<String> expansionCompletions = expansionManager.getTabCompletions(sender, args);
            if (expansionCompletions != null && !expansionCompletions.isEmpty()) {
                return expansionCompletions;
            }
        }

        return null;
    }

    public void help(CommandSender sender) {
        Map<String, String> repl = new HashMap<String, String>();
        repl.put("%VERSION%", VoiidCountdownTimer.getInstance().getDescription().getVersion());
        getMessagesManager().sendSection(sender, "command.help", true, repl);

        ExpansionManager expansionManager = getExpansionManager();
        if (expansionManager != null) {
            for (String line : expansionManager.getHelpLines()) {
                sender.sendMessage(getMessagesManager().color(line));
            }
        }
    }

    public boolean hasAdmin(CommandSender sender) {
        return sender.isOp() || sender.hasPermission(ADMIN_PERMISSION);
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (hasAdmin(sender)) {
            return true;
        }
        return permission != null && sender.hasPermission(permission);
    }

    public MessagesManager getMessagesManager() {
        return VoiidCountdownTimer.getMessagesManager();
    }

    public ExpansionManager getExpansionManager() {
        return VoiidCountdownTimer.getExpansionManager();
    }

    public List<String> getTimersCompletions(String[] args, int argTimerPos, boolean onlyEnabled) {
        List<String> completions = new ArrayList<String>();
        String argTimer = args[argTimerPos].toLowerCase();

        Map<String, TimerConfig> timers = VoiidCountdownTimer.getConfigsManager().getAllTimerConfigs();
        if (timers != null) {
            for (Map.Entry<String, TimerConfig> entry : timers.entrySet()) {
                String id = entry.getKey();
                TimerConfig cfg = entry.getValue();
                if (cfg == null) continue;
                if (onlyEnabled && !cfg.isEnabled()) continue;
                if (argTimer.isEmpty() || id.toLowerCase().startsWith(argTimer)) {
                    completions.add(id);
                }
            }
        }

        return completions.isEmpty() ? null : completions;
    }
}
