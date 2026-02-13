package voiidstudios.vct.commands.subcommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.VCTEvent;
import voiidstudios.vct.commands.VCTSubcommand;
import voiidstudios.vct.managers.TimerManager;

import java.util.List;

public class ResumeSubcommand implements VCTSubcommand {
    private final SubcommandContext context;

    public ResumeSubcommand(SubcommandContext context) { this.context = context; }
    public String name() { return "resume"; }
    public String permission() { return "voiidcountdowntimer.command.resume"; }

    public boolean execute(CommandSender sender, String[] args) {
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            context.getMessagesManager().send(sender, "timer_status.not_exists");
            return true;
        }
        timer.resume();
        context.getMessagesManager().send(sender, "timer_status.resume");
        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.RESUME, sender));
        return true;
    }

    public List<String> tabComplete(CommandSender sender, String[] args) { return null; }
}
