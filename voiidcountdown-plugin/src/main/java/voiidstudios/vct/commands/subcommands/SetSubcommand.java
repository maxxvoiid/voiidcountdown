package voiidstudios.vct.commands.subcommands;

import org.bukkit.command.CommandSender;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.VCTActions;
import voiidstudios.vct.commands.VCTSubcommand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetSubcommand implements VCTSubcommand {
    private final SubcommandContext context;

    public SetSubcommand(SubcommandContext context) { this.context = context; }
    public String name() { return "set"; }
    public String permission() { return "voiidcountdowntimer.command.set"; }

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length < 2) {
            context.getMessagesManager().send(sender, "timer.set.error");
            return true;
        }

        String timeHHMMSS = args[1];
        String timerId = (args.length >= 3) ? args[2] : null;
        Timer timer = VCTActions.createTimer(timeHHMMSS, timerId, sender);
        if (timer == null) {
            context.getMessagesManager().send(sender, "timer.set.format_incorrect");
            return true;
        }

        Map<String, String> repl = new HashMap<String, String>();
        repl.put("%HH%", String.format("%02d", Integer.parseInt(timer.getTimeLeftHH())));
        repl.put("%MM%", String.format("%02d", Integer.parseInt(timer.getTimeLeftMM())));
        repl.put("%SS%", String.format("%02d", Integer.parseInt(timer.getTimeLeftSS())));
        context.getMessagesManager().send(sender, "timer_status.start", repl);
        return true;
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> out = new ArrayList<String>();
            out.add("<HH:MM:SS>");
            return out;
        }
        if (args.length == 3) {
            return context.getMainCommand().getTimersCompletions(args, 2, true);
        }
        return null;
    }
}
