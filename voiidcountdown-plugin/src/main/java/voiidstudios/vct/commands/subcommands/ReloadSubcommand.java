package voiidstudios.vct.commands.subcommands;

import org.bukkit.command.CommandSender;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.commands.VCTSubcommand;

import java.util.List;

public class ReloadSubcommand implements VCTSubcommand {
    private final SubcommandContext context;

    public ReloadSubcommand(SubcommandContext context) { this.context = context; }
    public String name() { return "reload"; }
    public String permission() { return "voiidcountdowntimer.command.reload"; }
    public boolean execute(CommandSender sender, String[] args) {
        VoiidCountdownTimer.getConfigsManager().reload();
        context.getMessagesManager().send(sender, "command.reload");
        Timer.refreshTimerText();
        return true;
    }
    public List<String> tabComplete(CommandSender sender, String[] args) { return null; }
}
