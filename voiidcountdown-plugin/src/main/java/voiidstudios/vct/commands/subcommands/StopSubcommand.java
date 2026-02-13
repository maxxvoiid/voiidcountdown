package voiidstudios.vct.commands.subcommands;

import org.bukkit.command.CommandSender;
import voiidstudios.vct.commands.VCTSubcommand;
import voiidstudios.vct.managers.TimerManager;

import java.util.List;

public class StopSubcommand implements VCTSubcommand {
    public String name() { return "stop"; }
    public String permission() { return "voiidcountdowntimer.command.stop"; }
    public boolean execute(CommandSender sender, String[] args) {
        TimerManager.getInstance().deleteTimer(sender);
        return true;
    }
    public List<String> tabComplete(CommandSender sender, String[] args) { return null; }
}
