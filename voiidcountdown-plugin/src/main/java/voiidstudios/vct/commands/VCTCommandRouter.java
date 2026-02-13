package voiidstudios.vct.commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VCTCommandRouter {
    private final MainCommand mainCommand;
    private final Map<String, VCTSubcommand> subcommands = new LinkedHashMap<String, VCTSubcommand>();

    public VCTCommandRouter(MainCommand mainCommand) {
        this.mainCommand = mainCommand;
    }

    public void register(VCTSubcommand subcommand) {
        subcommands.put(subcommand.name().toLowerCase(Locale.ROOT), subcommand);
    }

    public boolean dispatch(CommandSender sender, String[] args) {
        if (args.length < 1) {
            return false;
        }

        VCTSubcommand subcommand = subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subcommand == null) {
            return false;
        }

        if (!mainCommand.hasPermission(sender, subcommand.permission())) {
            mainCommand.getMessagesManager().send(sender, "command.no_permissions");
            return true;
        }

        return subcommand.execute(sender, args);
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<String>();
            String search = args[0].toLowerCase(Locale.ROOT);
            for (VCTSubcommand subcommand : subcommands.values()) {
                if (!mainCommand.hasAdmin(sender)) {
                    continue;
                }
                String name = subcommand.name();
                if (search.isEmpty() || name.startsWith(search)) {
                    completions.add(name);
                }
            }
            return completions;
        }

        VCTSubcommand subcommand = subcommands.get(args[0].toLowerCase(Locale.ROOT));
        if (subcommand == null) {
            return null;
        }

        if (!mainCommand.hasAdmin(sender)) {
            return null;
        }

        return subcommand.tabComplete(sender, args);
    }
}
