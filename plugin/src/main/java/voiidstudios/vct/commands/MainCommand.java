package voiidstudios.vct.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.api.Timer;
import voiidstudios.vct.api.VCTActions;
import voiidstudios.vct.api.VCTEvent;
import voiidstudios.vct.configs.model.TimerConfig;
import voiidstudios.vct.expansions.ExpansionManager;
import voiidstudios.vct.expansions.ExpansionMetadata;
import voiidstudios.vct.expansions.ScriptExpansion;
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TimerManager;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainCommand implements CommandExecutor, TabCompleter {
    private static final String ADMIN_PERMISSION = "voiidcountdowntimer.admin";
    private static final Map<String, String> COMMAND_PERMISSIONS = new LinkedHashMap<>();
    private static final List<String> ROOT_COMMANDS = new ArrayList<>();

    static {
        COMMAND_PERMISSIONS.put("help", "voiidcountdowntimer.command.help");
        COMMAND_PERMISSIONS.put("reload", "voiidcountdowntimer.command.reload");
        COMMAND_PERMISSIONS.put("set", "voiidcountdowntimer.command.set");
        COMMAND_PERMISSIONS.put("pause", "voiidcountdowntimer.command.pause");
        COMMAND_PERMISSIONS.put("resume", "voiidcountdowntimer.command.resume");
        COMMAND_PERMISSIONS.put("stop", "voiidcountdowntimer.command.stop");
        COMMAND_PERMISSIONS.put("modify", "voiidcountdowntimer.command.modify");
        COMMAND_PERMISSIONS.put("expansion", "voiidcountdowntimer.command.expansion");

        ROOT_COMMANDS.addAll(COMMAND_PERMISSIONS.keySet());
    }

    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();
        ExpansionManager expansionManager = VoiidCountdownTimer.getExpansionManager();

        if (args.length >= 1) {
            String root = args[0].toLowerCase(Locale.ROOT);
            if (ROOT_COMMANDS.contains(root) && !hasCommandPermission(sender, root)) {
                msgManager.send(sender, "command.no_permissions");
                return true;
            }

            if (root.equals("help")){
                help(sender);
                return true;
            }else if (root.equals("reload")){
                reload(sender, msgManager);
                return true;
            }else if (root.equals("set")){
                set(sender, args, msgManager);
                return true;
            }else if (root.equals("pause")){
                pause(sender, msgManager);
                return true;
            }else if (root.equals("resume")){
                resume(sender, msgManager);
                return true;
            }else if (root.equals("stop")){
                stop(sender);
                return true;
            }else if (root.equals("modify")){
                modify(sender, args, msgManager);
                return true;
            }else if (root.equals("expansion")){
                expansion(sender, args, expansionManager, msgManager);
                return true;
            }
        }

        if (expansionManager != null && expansionManager.executeCommand(sender, args)) {
            return true;
        }

        if (!hasCommandPermission(sender, "help")) {
            msgManager.send(sender, "command.no_permissions");
            return true;
        }

        help(sender);

        return true;
    }

    public void reload(CommandSender sender, MessagesManager msgManager){
        VoiidCountdownTimer.getConfigsManager().reload();
        msgManager.send(sender, "command.reload");
        Timer.refreshTimerText();
    }

    public void set(CommandSender sender, String[] args, MessagesManager msgManager){
        if (args.length < 2) {
            msgManager.send(sender, "timer.set.error");
            return;
        }

        String timeHHMMSS = args[1];
        String timerId = (args.length >= 3) ? args[2] : null;

        Timer timer = VCTActions.createTimer(timeHHMMSS, timerId, sender);
        if (timer == null) {
            msgManager.send(sender, "timer.set.format_incorrect");
            return;
        }

        Map<String, String> repl = new HashMap<>();
        repl.put("%HH%", String.format("%02d", Integer.parseInt(timer.getTimeLeftHH())));
        repl.put("%MM%", String.format("%02d", Integer.parseInt(timer.getTimeLeftMM())));
        repl.put("%SS%", String.format("%02d", Integer.parseInt(timer.getTimeLeftSS())));

        msgManager.send(sender, "timer_status.start", repl);
    }

    public void pause(CommandSender sender, MessagesManager msgManager){
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            msgManager.send(sender, "timer_status.not_exists");
            return;
        }
        timer.pause();
        msgManager.send(sender, "timer_status.pause");

        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.PAUSE, sender));
    }

    public void resume(CommandSender sender, MessagesManager msgManager){
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            msgManager.send(sender, "timer_status.not_exists");
            return;
        }
        timer.resume();
        msgManager.send(sender, "timer_status.resume");

        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.RESUME, sender));
    }

    public void stop(CommandSender sender){
        TimerManager.getInstance().deleteTimer(sender);
    }

    public void modify(CommandSender sender, String[] args, MessagesManager msgManager) {
        java.util.List<String> parts;
        int addHours, addMinutes, addSeconds, totalSecondsToAdd;
        Timer timer;
        int setHours, setMinutes, setSeconds, totalSecondsToSet;
        int takeHours, takeMinutes, takeSeconds, totalSecondsToTake;

        if (args.length < 2) {
            msgManager.sendSection(sender, "timer.modify.help", true, null);
            return;
        }

        timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            msgManager.send(sender, "timer_status.not_exists");
            return;
        }

        String modifier = args[1].toLowerCase();
        switch (modifier) {
            case "add":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.add.error");
                    return;
                }

                totalSecondsToAdd = VCTActions.helper_parseTimeToSeconds(args[2]);

                addHours = 0;
                addMinutes = 0;
                addSeconds = 0;

                addHours = totalSecondsToAdd / 3600;
                addMinutes = (totalSecondsToAdd % 3600) / 60;
                addSeconds = totalSecondsToAdd % 60;

                if (addHours < 0 || addMinutes < 0 || addMinutes > 59 || addSeconds < 0 || addSeconds > 59) {
                    msgManager.send(sender, "timer.set.format_incorrect");
                    return;
                }

                if (totalSecondsToAdd == 0) {
                    msgManager.send(sender, "timer.set.format_out_range");
                    return;
                }

                boolean addSuccess = VCTActions.modifyTimer("add", args[2], sender);
                if (!addSuccess) {
                    msgManager.send(sender, "timer_status.not_exists");
                    return;
                }

                Map<String, String> addRepl = new HashMap<>();
                addRepl.put("%HH%", String.format("%02d", addHours));
                addRepl.put("%MM%", String.format("%02d", addMinutes));
                addRepl.put("%SS%", String.format("%02d", addSeconds));

                msgManager.send(sender, "timer.modify.add.success", addRepl);
                return;
            case "set":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.set.error");
                    return;
                }

                totalSecondsToSet = VCTActions.helper_parseTimeToSeconds(args[2]);

                setHours = 0;
                setMinutes = 0;
                setSeconds = 0;

                setHours = totalSecondsToSet / 3600;
                setMinutes = (totalSecondsToSet % 3600) / 60;
                setSeconds = totalSecondsToSet % 60;

                if (setHours < 0 || setMinutes < 0 || setMinutes > 59 || setSeconds < 0 || setSeconds > 59) {
                    msgManager.send(sender, "timer.set.format_incorrect");
                    return;
                }

                boolean setSuccess = VCTActions.modifyTimer("set", args[2], sender);
                if (!setSuccess) {
                    msgManager.send(sender, "timer_status.not_exists");
                    return;
                }

                Map<String, String> setRepl = new HashMap<>();
                setRepl.put("%HH%", String.format("%02d", setHours));
                setRepl.put("%MM%", String.format("%02d", setMinutes));
                setRepl.put("%SS%", String.format("%02d", setSeconds));

                msgManager.send(sender, "timer.modify.set.success", setRepl);
                return;
            case "take":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.take.error");
                    return;
                }

                totalSecondsToTake = VCTActions.helper_parseTimeToSeconds(args[2]);

                takeHours = 0;
                takeMinutes = 0;
                takeSeconds = 0;

                takeHours = totalSecondsToTake / 3600;
                takeMinutes = (totalSecondsToTake % 3600) / 60;
                takeSeconds = totalSecondsToTake % 60;

                if (takeHours < 0 || takeMinutes < 0 || takeMinutes > 59 || takeSeconds < 0 || takeSeconds > 59) {
                    msgManager.send(sender, "timer.set.format_incorrect");
                    return;
                }

                boolean takeSuccess = VCTActions.modifyTimer("take", args[2], sender);
                if (!takeSuccess) {
                    msgManager.send(sender, "timer_status.not_exists");
                    return;
                }

                Map<String, String> takeRepl = new HashMap<>();
                takeRepl.put("%HH%", String.format("%02d", takeHours));
                takeRepl.put("%MM%", String.format("%02d", takeMinutes));
                takeRepl.put("%SS%", String.format("%02d", takeSeconds));

                msgManager.send(sender, "timer.modify.take.success", takeRepl);
                return;
            case "bossbar_color":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.bossbar_color.error");
                    return;
                }
                String colorName = args[2].toUpperCase();

                boolean bcSuccess = VCTActions.modifyTimer("bossbar_color", colorName, sender);

                if (bcSuccess) {
                    Map<String, String> barcolorRepl = new HashMap<>();
                    barcolorRepl.put("%TIMER%", timer.getTimerId());
                    barcolorRepl.put("%COLOR%", colorName);

                    msgManager.send(sender, "timer.modify.bossbar_color.success", barcolorRepl);
                } else {
                    Map<String, String> barcolorInvRepl = new HashMap<>();
                    barcolorInvRepl.put("%COLOR%", colorName);

                    msgManager.send(sender, "timer.modify.bossbar_color.invalid", barcolorInvRepl);
                }
                return;
            case "bossbar_style":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.bossbar_style.error");
                    return;
                }
                String styleName = args[2].toUpperCase();

                boolean bsSuccess = VCTActions.modifyTimer("bossbar_style", styleName, sender);

                if (bsSuccess) {
                    Map<String, String> barstyleRepl = new HashMap<>();
                    barstyleRepl.put("%TIMER%", timer.getTimerId());
                    barstyleRepl.put("%STYLE%", styleName);

                    msgManager.send(sender, "timer.modify.bossbar_style.success", barstyleRepl);
                } else {
                    Map<String, String> barstyleInvRepl = new HashMap<>();
                    barstyleInvRepl.put("%STYLE%", styleName);

                    msgManager.send(sender, "timer.modify.bossbar_style.invalid", barstyleInvRepl);
                }
                return;
            case "sound":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.sound.error");
                    return;
                }

                parts = new java.util.ArrayList<>();
                for (int i = 2; i < args.length; i++) {
                    parts.add(args[i]);
                }
                String rawSound = String.join(" ", parts).trim();

                if (rawSound.startsWith("\"") && rawSound.endsWith("\"") && rawSound.length() >= 2) {
                    rawSound = rawSound.substring(1, rawSound.length() - 1).trim();
                } else {
                    msgManager.send(sender, "timer.modify.sound.require_quotes");
                    return;
                }

                if (rawSound.isEmpty()) return;

                boolean isVanillaSound = false;
                try {
                    String enumName = rawSound.toUpperCase(java.util.Locale.ROOT).replace(':', '_');
                    org.bukkit.Sound.valueOf(enumName);
                    isVanillaSound = true;
                } catch (IllegalArgumentException ignored) {}

                boolean soundSuccess = VCTActions.modifyTimer("sound", rawSound, sender);

                if (soundSuccess) {
                    Map<String, String> soundRepl = new HashMap<>();
                    soundRepl.put("%TIMER%", timer.getTimerId());
                    soundRepl.put("%SOUND%", rawSound);
                    soundRepl.put("%TYPE%", isVanillaSound ? "vanilla" : "custom");

                    msgManager.send(sender, "timer.modify.sound.success", soundRepl);
                } else {
                    msgManager.send(sender, "timer.modify.sound.error");
                }
                return;
            case "sound_enable":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.sound_enable.error");
                    return;
                }

                String value = args[2].toLowerCase();

                boolean seSuccess = VCTActions.modifyTimer("sound_enable", value, sender);

                if (seSuccess) {
                    Map<String, String> soundenableRepl = new HashMap<>();
                    soundenableRepl.put("%TIMER%", timer.getTimerId());
                    soundenableRepl.put("%SOUNDENABLE%", value);

                    msgManager.send(sender, "timer.modify.sound_enable.success", soundenableRepl);
                } else {
                    msgManager.send(sender, "timer.modify.sound_enable.invalid");
                }
                return;
            case "sound_volume":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.sound_volume.error");
                    return;
                }

                float newVolume;
                try {
                    newVolume = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    msgManager.send(sender, "timer.modify.sound_volume.invalid");
                    return;
                }

                if (newVolume < 0.1f || newVolume > 2.0f) {
                    msgManager.send(sender, "timer.modify.sound_volume.out_range");
                    return;
                }

                boolean svSuccess = VCTActions.modifyTimer("sound_volume", String.valueOf(newVolume), sender);

                if (svSuccess) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("%TIMER%", timer.getTimerId());
                    repl.put("%VOLUME%", String.valueOf(newVolume));

                    msgManager.send(sender, "timer.modify.sound_volume.success", repl);
                }
                return;
            case "sound_pitch":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.sound_pitch.error");
                    return;
                }

                float newPitch;
                try {
                    newPitch = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    msgManager.send(sender, "timer.modify.sound_pitch.invalid");
                    return;
                }

                if (newPitch < 0.1f || newPitch > 2.0f) {
                    msgManager.send(sender, "timer.modify.sound_pitch.out_range");
                    return;
                }

                boolean spSuccess = VCTActions.modifyTimer("sound_pitch", String.valueOf(newPitch), sender);

                if (spSuccess) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("%TIMER%", timer.getTimerId());
                    repl.put("%PITCH%", String.valueOf(newPitch));

                    msgManager.send(sender, "timer.modify.sound_pitch.success", repl);
                }
                return;
            case "text":
                if (args.length < 3) {
                    msgManager.send(sender, "timer.modify.text.error");
                    return;
                }

                parts = new java.util.ArrayList<>();
                for (int i = 2; i < args.length; i++) {
                    parts.add(args[i]);
                }
                String rawText = String.join(" ", parts).trim();

                if (rawText.startsWith("\"") && rawText.endsWith("\"") && rawText.length() >= 2) {
                    rawText = rawText.substring(1, rawText.length() - 1);
                } else {
                    msgManager.send(sender, "timer.modify.text.require_quotes");
                    return;
                }

                if (rawText.isEmpty()) return;

                boolean textSuccess = VCTActions.modifyTimer("text", rawText, sender);

                if (textSuccess) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("%TIMER%", timer.getTimerId());
                    repl.put("%TEXT%", rawText);

                    msgManager.send(sender, "timer.modify.text.success", repl);
                }
                return;
        }

        msgManager.send(sender, "timer.modify.invalid");
    }
    
    private void expansion(CommandSender sender, String[] args, ExpansionManager expansionManager, MessagesManager msgManager) {
        if (expansionManager == null) {
            msgManager.send(sender, "expansion.disabled");
            return;
        }

        if (args.length < 2) {
            msgManager.sendSection(sender, "expansion.help", true, null);
            return;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        String targetName = args.length >= 3 ? args[2] : null;

        switch (action) {
            case "info":
                if (targetName == null) {
                    msgManager.send(sender, "expansion.no_id_specified");
                    return;
                }

                ExpansionMetadata infoMetadata = expansionManager.getExpansionMetadata(targetName);
                if (infoMetadata == null) {
                    Map<String, String> expansionInfoNotFoundRepl = new HashMap<>();
                    expansionInfoNotFoundRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.not_found", expansionInfoNotFoundRepl);
                    return;
                }

                boolean infoLoaded = expansionManager.isExpansionLoaded(infoMetadata.getName());
                List<String> authors = infoMetadata.getAuthors();
                String authorsText = authors.isEmpty() ? "N/A" : String.join("&7, &f", authors);
                String description = infoMetadata.getDescription();

                if (description == null || description.trim().isEmpty()) {
                    description = "No description.";
                }

                Map<String, String> repl = new HashMap<>();
                repl.put("%EXPANSION%", infoMetadata.getName());
                repl.put("%STATUS%", infoLoaded ? "Enabled" : "Disabled");
                repl.put("%STATUSCOLOR%", infoLoaded ? "&a" : "&c");
                repl.put("%AUTHORS%", authorsText);
                repl.put("%VERSION%", infoMetadata.getVersion());
                repl.put("%DESCRIPTION%", description);

                msgManager.sendSection(sender, "expansion.info", true, repl);
                return;
            case "enable":
                if (targetName == null) {
                    msgManager.send(sender, "expansion.no_id_specified");
                    return;
                }

                ExpansionMetadata enableMetadata = expansionManager.getExpansionMetadata(targetName);
                if (enableMetadata == null) {
                    Map<String, String> expansionEnableNotFoundRepl = new HashMap<>();
                    expansionEnableNotFoundRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.not_found", expansionEnableNotFoundRepl);
                    return;
                }

                if (expansionManager.isExpansionLoaded(enableMetadata.getName())) {
                    Map<String, String> expansionEnableAlreadyEnabledRepl = new HashMap<>();
                    expansionEnableAlreadyEnabledRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.enable.already_enabled", expansionEnableAlreadyEnabledRepl);
                    return;
                }

                if (expansionManager.enableExpansion(enableMetadata.getName())) {
                    Map<String, String> expansionEnableRepl = new HashMap<>();
                    expansionEnableRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.enable.success", expansionEnableRepl);
                } else {
                    Map<String, String> expansionEnableErrorRepl = new HashMap<>();
                    expansionEnableErrorRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.enable.error", expansionEnableErrorRepl);
                }
                return;
            case "disable":
                if (targetName == null) {
                    msgManager.send(sender, "expansion.no_id_specified");
                    return;
                }

                ExpansionMetadata disableMetadata = expansionManager.getExpansionMetadata(targetName);
                if (disableMetadata == null) {
                    Map<String, String> expansionDisableNotFoundRepl = new HashMap<>();
                    expansionDisableNotFoundRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.not_found", expansionDisableNotFoundRepl);
                    return;
                }

                if (!expansionManager.isExpansionLoaded(disableMetadata.getName())) {
                    Map<String, String> expansionDisableAlreadyDisabledRepl = new HashMap<>();
                    expansionDisableAlreadyDisabledRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.disable.already_disabled", expansionDisableAlreadyDisabledRepl);
                    return;
                }

                if (expansionManager.disableExpansion(disableMetadata.getName())) {
                    Map<String, String> expansionDisableRepl = new HashMap<>();
                    expansionDisableRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.disable.success", expansionDisableRepl);
                } else {
                    Map<String, String> expansionDisableErrorRepl = new HashMap<>();
                    expansionDisableErrorRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.disable.error", expansionDisableErrorRepl);
                }
                return;
            case "reload":
                if (targetName == null) {
                    msgManager.send(sender, "expansion.no_id_specified");
                    return;
                }

                ExpansionMetadata reloadMetadata = expansionManager.getExpansionMetadata(targetName);
                if (reloadMetadata == null) {
                    Map<String, String> expansionReloadNotFoundRepl = new HashMap<>();
                    expansionReloadNotFoundRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.not_found", expansionReloadNotFoundRepl);
                    return;
                }

                if (!expansionManager.isExpansionLoaded(reloadMetadata.getName())) {
                    Map<String, String> expansionReloadDisabledRepl = new HashMap<>();
                    expansionReloadDisabledRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.reload.disabled", expansionReloadDisabledRepl);
                    return;
                }

                if (expansionManager.reloadExpansion(reloadMetadata.getName())) {
                    Map<String, String> expansionReloadRepl = new HashMap<>();
                    expansionReloadRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.reload.success", expansionReloadRepl);
                } else {
                    Map<String, String> expansionReloadErrorRepl = new HashMap<>();
                    expansionReloadErrorRepl.put("%EXPANSION%", targetName);

                    msgManager.send(sender, "expansion.reload.error", expansionReloadErrorRepl);
                }
                return;
            case "reloadall":
                int reloaded = expansionManager.reloadAllExpansions();
                if (reloaded == 0) {
                    msgManager.send(sender, "expansion.reloadall.error");
                } else {
                    Map<String, String> expansionReloadallRepl = new HashMap<>();
                    expansionReloadallRepl.put("%EXPANSIONS%", String.valueOf(reloaded));

                    msgManager.send(sender, "expansion.reloadall.success", expansionReloadallRepl);
                }
                return;
            case "list":
                List<String> known = expansionManager.getKnownExpansionNames();

                Map<String, String> expansionListTitleRepl = new HashMap<>();
                expansionListTitleRepl.put("%EXPANSIONS%", String.valueOf(known.size()));

                String hoverText = msgManager.getListAsSingleString("expansion.list.hint", null);

                TextComponent infoIcon = new TextComponent("ℹ ");
                infoIcon.setColor(ChatColor.AQUA);
                infoIcon.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(hoverText).create()
                ));
                infoIcon.setClickEvent(new ClickEvent(
                    ClickEvent.Action.OPEN_URL,
                    "https://vctdocs.mintlify.app/usage-guide/expansions"
                ));

                String prefixColored = ChatColor.translateAlternateColorCodes('&', VoiidCountdownTimer.prefix);
                TextComponent message = new TextComponent(prefixColored);
                message.addExtra(infoIcon);
                String titleColored = ChatColor.translateAlternateColorCodes('&', 
                    msgManager.getTranslated("expansion.list.title", expansionListTitleRepl, false)
                );
                message.addExtra(new TextComponent(titleColored));

                sender.spigot().sendMessage(message);

                if (known.isEmpty()) {
                    msgManager.send(sender, "expansion.list.no_extensions");
                } else {
                    TextComponent listComponent = new TextComponent("");
                    boolean first = true;

                    for (String name : known) {
                        boolean active = expansionManager.isExpansionLoaded(name);
                        ChatColor color = active ? ChatColor.GREEN : ChatColor.RED;
                        String status = active ? msgManager.getTranslated("expansion.list.enabled", null, false) : msgManager.getTranslated("expansion.list.disabled", null, false);

                        TextComponent expansionComponent = new TextComponent((first ? "" : ChatColor.GRAY + ", ") + color + name);
                        expansionComponent.setClickEvent(new ClickEvent(
                            ClickEvent.Action.RUN_COMMAND,
                            "/vct expansion info " + name
                        ));
                        expansionComponent.setHoverEvent(new HoverEvent(
                            HoverEvent.Action.SHOW_TEXT,
                            new ComponentBuilder(status).create()
                        ));

                        listComponent.addExtra(expansionComponent);
                        first = false;
                    }

                    sender.spigot().sendMessage(listComponent);
                }
                return;
            default:
                msgManager.send(sender, "expansion.reloadall.invalid");
        }
    }

    public void help(CommandSender sender){
        Map<String, String> repl = new HashMap<>();
        repl.put("%VERSION%", VoiidCountdownTimer.getInstance().getDescription().getVersion());

        VoiidCountdownTimer.getMessagesManager().sendSection(sender, "command.help", true, repl);

        ExpansionManager expansionManager = VoiidCountdownTimer.getExpansionManager();
        if (expansionManager != null) {
            for (String line : expansionManager.getHelpLines()) {
                sender.sendMessage(
                        VoiidCountdownTimer.getMessagesManager().color(line)
                );
            }
        }
    }

    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String label, String[] args){
        ExpansionManager expansionManager = VoiidCountdownTimer.getExpansionManager();
        boolean hasAdmin = sender.isOp() || sender.hasPermission("voiidcountdowntimer.admin");

        if (args.length == 1) {
            java.util.Set<String> completions = new java.util.LinkedHashSet<>();

            if (hasAdmin) {
                List<String> commands = new ArrayList<String>();
                commands.add("help");commands.add("reload");
                commands.add("set");commands.add("pause");
                commands.add("resume");commands.add("stop");
                commands.add("modify");commands.add("expansion");
                for(String c : commands) {
                    if(args[0].isEmpty() || c.startsWith(args[0].toLowerCase())) {
                        completions.add(c);
                    }
                }
            }

            if (expansionManager != null) {
                completions.addAll(expansionManager.getRootSuggestions(args[0]));
            }

            return completions.isEmpty() ? null : new ArrayList<>(completions);
        }

        if (hasAdmin) {
            if (args.length == 2){
                List<String> subcompletions = new ArrayList<String>();
                List<String> subcommands = new ArrayList<String>();

                if(args[0].equalsIgnoreCase("modify")) {
                    subcommands.add("add");subcommands.add("set");
                    subcommands.add("take");subcommands.add("bossbar_color");
                    subcommands.add("bossbar_style");subcommands.add("sound");
                    subcommands.add("sound_enable");subcommands.add("sound_volume");
                    subcommands.add("sound_pitch");subcommands.add("text");
                }else if(args[0].equalsIgnoreCase("set")){
                    subcommands.add("<HH:MM:SS>");
                }else if(args[0].equalsIgnoreCase("expansion")){
                    subcommands.add("info");subcommands.add("enable");
                    subcommands.add("disable");subcommands.add("reload");
                    subcommands.add("reloadall");subcommands.add("list");
                }

                for(String c : subcommands) {
                    if(args[1].isEmpty() || c.startsWith(args[1].toLowerCase())) {
                        subcompletions.add(c);
                    }
                }

                if(!subcompletions.isEmpty()) return subcompletions;
            } else if (args.length == 3){
                if (args[0].equalsIgnoreCase("expansion") && expansionManager != null) {
                    String action = args[1].toLowerCase(Locale.ROOT);
                    if (!action.equals("reloadall")) {
                        List<String> names = expansionManager.getKnownExpansionNames();
                        List<String> matches = new ArrayList<>();
                        for (String name : names) {
                            if (args[2].isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))) {
                                matches.add(name);
                            }
                        }
                        if (!matches.isEmpty()) {
                            return matches;
                        }
                    }
                }

                List<String> subcompletions = new ArrayList<String>();
                List<String> subcommands = new ArrayList<String>();

                if(args[0].equalsIgnoreCase("modify")) {
                    if(args[1].equalsIgnoreCase("bossbar_color")){
                        subcommands.add("BLUE");subcommands.add("GREEN");
                        subcommands.add("PINK");subcommands.add("PURPLE");
                        subcommands.add("RED");subcommands.add("WHITE");
                        subcommands.add("YELLOW");
                    }else if(args[1].equalsIgnoreCase("bossbar_style")){
                        subcommands.add("SOLID");subcommands.add("SEGMENTED_6");
                        subcommands.add("SEGMENTED_10");subcommands.add("SEGMENTED_12");
                        subcommands.add("SEGMENTED_20");
                    }else if(args[1].equalsIgnoreCase("sound")){
                        subcommands.add("<\"sound in quotes\">");
                    }else if(args[1].equalsIgnoreCase("sound_enable")){
                        subcommands.add("true");subcommands.add("false");
                    }else if(args[1].equalsIgnoreCase("sound_volume") || args[1].equalsIgnoreCase("sound_pitch")){
                        subcommands.add("<0.1 - 2.0>");
                    }else if(args[1].equalsIgnoreCase("text")){
                        subcommands.add("<\"text in quotes\">");
                    }else if(args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("take")){
                        subcommands.add("<HH:MM:SS>");
                    }
                } else if(args[0].equalsIgnoreCase("set")){ 
                    List<String> timers = getTimersCompletions(args, 2, true);
                    if (timers != null) {
                        return timers;
                    }
                }

                for(String c : subcommands) {
                    if(args[2].isEmpty() || c.startsWith(args[1].toLowerCase())) {
                        subcompletions.add(c);
                    }
                }
                
                if(!subcompletions.isEmpty()) return subcompletions;
            }
        }

        if (expansionManager != null) {
            List<String> expansionCompletions = expansionManager.getTabCompletions(sender, args);
            if (expansionCompletions != null && !expansionCompletions.isEmpty()) {
                return expansionCompletions;
            }
        }

        return null;
    }

    private boolean hasCommandPermission(CommandSender sender, String commandKey) {
        if (sender.isOp() || sender.hasPermission(ADMIN_PERMISSION)) {
            return true;
        }

        String permission = COMMAND_PERMISSIONS.get(commandKey);
        if (permission == null) {
            return false;
        }

        return sender.hasPermission(permission);
    }

    public List<String> getTimersCompletions(String[] args, int argTimerPos, boolean onlyEnabled) {
        List<String> completions = new ArrayList<>();

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