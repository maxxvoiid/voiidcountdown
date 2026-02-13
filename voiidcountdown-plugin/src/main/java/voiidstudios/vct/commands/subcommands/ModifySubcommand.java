package voiidstudios.vct.commands.subcommands;

import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.VCTActions;
import voiidstudios.vct.commands.VCTSubcommand;
import voiidstudios.vct.managers.TimerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ModifySubcommand implements VCTSubcommand {
    private final SubcommandContext context;

    public ModifySubcommand(SubcommandContext context) { this.context = context; }
    public String name() { return "modify"; }
    public String permission() { return "voiidcountdowntimer.command.modify"; }

    public boolean execute(CommandSender sender, String[] args) {
        List<String> parts;
        if (args.length < 2) {
            context.getMessagesManager().sendSection(sender, "timer.modify.help", true, null);
            return true;
        }

        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            context.getMessagesManager().send(sender, "timer_status.not_exists");
            return true;
        }

        String modifier = args[1].toLowerCase(Locale.ROOT);
        switch (modifier) {
            case "add":
            case "set":
            case "take":
                if (args.length < 3) {
                    context.getMessagesManager().send(sender, "timer.modify." + modifier + ".error");
                    return true;
                }
                int total = VCTActions.helper_parseTimeToSeconds(args[2]);
                int hh = total / 3600;
                int mm = (total % 3600) / 60;
                int ss = total % 60;
                if (hh < 0 || mm < 0 || mm > 59 || ss < 0 || ss > 59) {
                    context.getMessagesManager().send(sender, "timer.set.format_incorrect");
                    return true;
                }
                if ("add".equals(modifier) && total == 0) {
                    context.getMessagesManager().send(sender, "timer.set.format_out_range");
                    return true;
                }
                if (!VCTActions.modifyTimer(modifier, args[2], sender)) {
                    context.getMessagesManager().send(sender, "timer_status.not_exists");
                    return true;
                }
                Map<String, String> timeRepl = new HashMap<String, String>();
                timeRepl.put("%HH%", String.format("%02d", hh));
                timeRepl.put("%MM%", String.format("%02d", mm));
                timeRepl.put("%SS%", String.format("%02d", ss));
                context.getMessagesManager().send(sender, "timer.modify." + modifier + ".success", timeRepl);
                return true;
            case "bossbar_color":
            case "bossbar_style":
                if (args.length < 3) { context.getMessagesManager().send(sender, "timer.modify." + modifier + ".error"); return true; }
                String value = args[2].toUpperCase(Locale.ROOT);
                boolean success = VCTActions.modifyTimer(modifier, value, sender);
                Map<String, String> repl = new HashMap<String, String>();
                repl.put("%TIMER%", timer.getTimerId());
                repl.put("bossbar_color".equals(modifier) ? "%COLOR%" : "%STYLE%", value);
                context.getMessagesManager().send(sender, success ? "timer.modify." + modifier + ".success" : "timer.modify." + modifier + ".invalid", repl);
                return true;
            case "sound":
                if (args.length < 3) { context.getMessagesManager().send(sender, "timer.modify.sound.error"); return true; }
                parts = new ArrayList<String>();
                for (int i = 2; i < args.length; i++) { parts.add(args[i]); }
                String rawSound = String.join(" ", parts).trim();
                if (rawSound.startsWith("\"") && rawSound.endsWith("\"") && rawSound.length() >= 2) {
                    rawSound = rawSound.substring(1, rawSound.length() - 1).trim();
                } else {
                    context.getMessagesManager().send(sender, "timer.modify.sound.require_quotes"); return true;
                }
                if (rawSound.isEmpty()) return true;
                boolean isVanillaSound = false;
                try { Sound.valueOf(rawSound.toUpperCase(Locale.ROOT).replace(':', '_')); isVanillaSound = true; } catch (IllegalArgumentException ignored) {}
                boolean soundSuccess = VCTActions.modifyTimer("sound", rawSound, sender);
                if (soundSuccess) {
                    Map<String, String> soundRepl = new HashMap<String, String>();
                    soundRepl.put("%TIMER%", timer.getTimerId());
                    soundRepl.put("%SOUND%", rawSound);
                    soundRepl.put("%TYPE%", isVanillaSound ? "vanilla" : "custom");
                    context.getMessagesManager().send(sender, "timer.modify.sound.success", soundRepl);
                } else {
                    context.getMessagesManager().send(sender, "timer.modify.sound.error");
                }
                return true;
            case "sound_enable":
                if (args.length < 3) { context.getMessagesManager().send(sender, "timer.modify.sound_enable.error"); return true; }
                String soundEnable = args[2].toLowerCase(Locale.ROOT);
                boolean seSuccess = VCTActions.modifyTimer("sound_enable", soundEnable, sender);
                if (seSuccess) {
                    Map<String, String> soundenableRepl = new HashMap<String, String>();
                    soundenableRepl.put("%TIMER%", timer.getTimerId());
                    soundenableRepl.put("%SOUNDENABLE%", soundEnable);
                    context.getMessagesManager().send(sender, "timer.modify.sound_enable.success", soundenableRepl);
                } else {
                    context.getMessagesManager().send(sender, "timer.modify.sound_enable.invalid");
                }
                return true;
            case "sound_volume":
            case "sound_pitch":
                if (args.length < 3) { context.getMessagesManager().send(sender, "timer.modify." + modifier + ".error"); return true; }
                float number;
                try { number = Float.parseFloat(args[2]); } catch (NumberFormatException e) { context.getMessagesManager().send(sender, "timer.modify." + modifier + ".invalid"); return true; }
                if (number < 0.1f || number > 2.0f) { context.getMessagesManager().send(sender, "timer.modify." + modifier + ".out_range"); return true; }
                if (VCTActions.modifyTimer(modifier, String.valueOf(number), sender)) {
                    Map<String, String> nr = new HashMap<String, String>();
                    nr.put("%TIMER%", timer.getTimerId());
                    nr.put("sound_volume".equals(modifier) ? "%VOLUME%" : "%PITCH%", String.valueOf(number));
                    context.getMessagesManager().send(sender, "timer.modify." + modifier + ".success", nr);
                }
                return true;
            case "text":
                if (args.length < 3) { context.getMessagesManager().send(sender, "timer.modify.text.error"); return true; }
                parts = new ArrayList<String>();
                for (int i = 2; i < args.length; i++) { parts.add(args[i]); }
                String rawText = String.join(" ", parts).trim();
                if (rawText.startsWith("\"") && rawText.endsWith("\"") && rawText.length() >= 2) {
                    rawText = rawText.substring(1, rawText.length() - 1);
                } else {
                    context.getMessagesManager().send(sender, "timer.modify.text.require_quotes"); return true;
                }
                if (rawText.isEmpty()) return true;
                if (VCTActions.modifyTimer("text", rawText, sender)) {
                    Map<String, String> textRepl = new HashMap<String, String>();
                    textRepl.put("%TIMER%", timer.getTimerId());
                    textRepl.put("%TEXT%", rawText);
                    context.getMessagesManager().send(sender, "timer.modify.text.success", textRepl);
                }
                return true;
            default:
                context.getMessagesManager().send(sender, "timer.modify.invalid");
                return true;
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> subcommands = new ArrayList<String>();
            subcommands.add("add"); subcommands.add("set"); subcommands.add("take"); subcommands.add("bossbar_color");
            subcommands.add("bossbar_style"); subcommands.add("sound"); subcommands.add("sound_enable");
            subcommands.add("sound_volume"); subcommands.add("sound_pitch"); subcommands.add("text");
            return filter(subcommands, args[1]);
        }
        if (args.length == 3) {
            List<String> subcommands = new ArrayList<String>();
            if (args[1].equalsIgnoreCase("bossbar_color")) {
                subcommands.add("BLUE"); subcommands.add("GREEN"); subcommands.add("PINK"); subcommands.add("PURPLE");
                subcommands.add("RED"); subcommands.add("WHITE"); subcommands.add("YELLOW");
            } else if (args[1].equalsIgnoreCase("bossbar_style")) {
                subcommands.add("SOLID"); subcommands.add("SEGMENTED_6"); subcommands.add("SEGMENTED_10"); subcommands.add("SEGMENTED_12"); subcommands.add("SEGMENTED_20");
            } else if (args[1].equalsIgnoreCase("sound")) {
                subcommands.add("<\"sound in quotes\">");
            } else if (args[1].equalsIgnoreCase("sound_enable")) {
                subcommands.add("true"); subcommands.add("false");
            } else if (args[1].equalsIgnoreCase("sound_volume") || args[1].equalsIgnoreCase("sound_pitch")) {
                subcommands.add("<0.1 - 2.0>");
            } else if (args[1].equalsIgnoreCase("text")) {
                subcommands.add("<\"text in quotes\">");
            } else if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("take")) {
                subcommands.add("<HH:MM:SS>");
            }
            return filter(subcommands, args[1]);
        }
        return null;
    }

    private List<String> filter(List<String> source, String startsWith) {
        List<String> output = new ArrayList<String>();
        String search = startsWith.toLowerCase(Locale.ROOT);
        for (String candidate : source) {
            if (startsWith.isEmpty() || candidate.toLowerCase(Locale.ROOT).startsWith(search)) {
                output.add(candidate);
            }
        }
        return output.isEmpty() ? null : output;
    }
}
