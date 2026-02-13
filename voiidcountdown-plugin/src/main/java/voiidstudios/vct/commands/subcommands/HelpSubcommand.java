package voiidstudios.vct.commands.subcommands;

import org.bukkit.command.CommandSender;
import voiidstudios.vct.commands.VCTSubcommand;

import java.util.List;

public class HelpSubcommand implements VCTSubcommand {
    private final SubcommandContext context;

    public HelpSubcommand(SubcommandContext context) {
        this.context = context;
    }

    public String name() { return "help"; }
    public String permission() { return "voiidcountdowntimer.command.help"; }
    public boolean execute(CommandSender sender, String[] args) { context.getMainCommand().help(sender); return true; }
    public List<String> tabComplete(CommandSender sender, String[] args) { return null; }
}
