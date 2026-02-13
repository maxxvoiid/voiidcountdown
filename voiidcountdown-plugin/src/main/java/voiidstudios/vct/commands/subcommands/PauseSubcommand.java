package voiidstudios.vct.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.VCTEvent;
import voiidstudios.vct.commands.VCTSubcommand;
import voiidstudios.vct.managers.TimerManager;

import java.util.List;

public class PauseSubcommand implements VCTSubcommand {
    private final SubcommandContext context;

    public PauseSubcommand(SubcommandContext context) { this.context = context; }
    public String name() { return "pause"; }
    public String permission() { return "voiidcountdowntimer.command.pause"; }

    public boolean execute(CommandSender sender, String[] args) {
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            context.getMessagesManager().send(sender, "timer_status.not_exists");
            return true;
        }
        timer.pause();
        context.getMessagesManager().send(sender, "timer_status.pause");
        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.PAUSE, sender));
        return true;
    }

    public List<String> tabComplete(CommandSender sender, String[] args) { return null; }
}
