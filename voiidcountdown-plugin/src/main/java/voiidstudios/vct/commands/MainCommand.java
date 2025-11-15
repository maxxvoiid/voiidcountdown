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
import voiidstudios.vct.managers.MessagesManager;
import voiidstudios.vct.managers.TimerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainCommand implements CommandExecutor, TabCompleter {
    public boolean onCommand(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();
        ExpansionManager expansionManager = VoiidCountdownTimer.getExpansionManager();

        boolean hasAdmin = sender.isOp() || sender.hasPermission("voiidcountdowntimer.admin");

        if (hasAdmin && args.length >= 1) {
            if(args[0].equalsIgnoreCase("help")){
                help(sender);
                return true;
            }else if (args[0].equalsIgnoreCase("reload")){
                reload(sender, args, msgManager, expansionManager);
                return true;
            }else if (args[0].equalsIgnoreCase("expansion")){
                handleExpansionCommand(sender, args, msgManager, expansionManager);
                return true;
            }else if (args[0].equalsIgnoreCase("set")){
                set(sender, args, msgManager);
                return true;
            }else if (args[0].equalsIgnoreCase("pause")){
                pause(sender, msgManager);
                return true;
            }else if (args[0].equalsIgnoreCase("resume")){
                resume(sender, msgManager);
                return true;
            }else if (args[0].equalsIgnoreCase("stop")){
                stop(sender);
                return true;
            }else if (args[0].equalsIgnoreCase("modify")){
                modify(sender, args, msgManager);
                return true;
            }
        }

        if (expansionManager != null && expansionManager.executeCommand(sender, args)) {
            return true;
        }

        if (!hasAdmin) {
            msgManager.sendConfigMessage(sender, "Messages.commandNoPermissions", true, null);
            return true;
        }

        help(sender);

        return true;
    }

    public void reload(CommandSender sender, String[] args, MessagesManager msgManager, ExpansionManager expansionManager){
        if (args.length < 2) {
            msgManager.sendConfigMessage(sender, "Messages.commandReloadUsage", true, null);
            return;
        }

        String target = args[1].toLowerCase(Locale.ROOT);
        switch (target) {
            case "config":
                VoiidCountdownTimer.getConfigsManager().reload();
                msgManager.sendConfigMessage(sender, "Messages.commandReload", true, null);
                Timer.refreshTimerText();
                break;
            case "expansion":
                if (expansionManager == null) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionUnavailable", true, null);
                    return;
                }

                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.commandReloadExpansionUsage", true, null);
                    return;
                }

                String expansionName = args[2];
                ExpansionManager.ExpansionActionResult result = expansionManager.reloadExpansion(expansionName);
                Map<String, String> repl = new HashMap<>();
                ExpansionMetadata metadata = expansionManager.getExpansionMetadata(expansionName);
                String displayName = metadata != null ? metadata.getName() : expansionName;
                repl.put("%EXPANSION%", displayName);

                if (result == ExpansionManager.ExpansionActionResult.SUCCESS) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionReloadSuccess", true, repl);
                } else if (result == ExpansionManager.ExpansionActionResult.NOT_FOUND) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionNotFound", true, repl);
                } else {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionReloadFailed", true, repl);
                }
                break;
            case "allexpansions":
                if (expansionManager == null) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionUnavailable", true, null);
                    return;
                }

                int reloaded = expansionManager.reloadAllExpansions();
                Map<String, String> allRepl = new HashMap<>();
                allRepl.put("%COUNT%", String.valueOf(reloaded));
                if (reloaded > 0) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionReloadAllSuccess", true, allRepl);
                } else {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionReloadAllFailed", true, null);
                }
                break;
            default:
                msgManager.sendConfigMessage(sender, "Messages.commandReloadUsage", true, null);
                break;
        }
    }

    private void handleExpansionCommand(CommandSender sender, String[] args, MessagesManager msgManager, ExpansionManager expansionManager) {
        if (expansionManager == null) {
            msgManager.sendConfigMessage(sender, "Messages.commandExpansionUnavailable", true, null);
            return;
        }

        if (args.length < 2) {
            msgManager.sendConfigMessage(sender, "Messages.commandExpansionUsage", true, null);
            return;
        }

        String sub = args[1].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "info":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionInfoUsage", true, null);
                    return;
                }

                String infoName = args[2];
                ExpansionMetadata infoMetadata = expansionManager.getExpansionMetadata(infoName);
                if (infoMetadata == null) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("%EXPANSION%", infoName);
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionNotFound", true, repl);
                    return;
                }

                boolean enabled = expansionManager.isExpansionLoaded(infoMetadata.getName());
                Map<String, String> headerRepl = new HashMap<>();
                headerRepl.put("%EXPANSION%", infoMetadata.getName());
                headerRepl.put("%STATUS%", enabled ? "&aEnabled" : "&cDisabled");
                msgManager.sendConfigMessage(sender, "Messages.commandExpansionInfoHeader", true, headerRepl);

                Map<String, String> versionRepl = new HashMap<>();
                versionRepl.put("%VERSION%", infoMetadata.getVersion());
                msgManager.sendConfigMessage(sender, "Messages.commandExpansionInfoVersion", false, versionRepl);

                String authors = String.join(", ", infoMetadata.getAuthors());
                if (authors.trim().isEmpty()) {
                    authors = "Unknown";
                }
                Map<String, String> authorsRepl = new HashMap<>();
                authorsRepl.put("%AUTHORS%", authors);
                msgManager.sendConfigMessage(sender, "Messages.commandExpansionInfoAuthors", false, authorsRepl);

                String description = infoMetadata.getDescription() == null ? "" : infoMetadata.getDescription().trim();
                if (description.isEmpty()) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionInfoNoDescription", false, null);
                } else {
                    Map<String, String> descriptionRepl = new HashMap<>();
                    descriptionRepl.put("%DESCRIPTION%", description);
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionInfoDescription", false, descriptionRepl);
                }
                break;
            case "enable":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionEnableUsage", true, null);
                    return;
                }

                handleExpansionStateChange(sender, expansionManager, expansionManager.enableExpansion(args[2]), args[2], msgManager,
                        "Messages.commandExpansionEnableSuccess",
                        "Messages.commandExpansionEnableAlready",
                        "Messages.commandExpansionNotFound",
                        "Messages.commandExpansionEnableFailed");
                break;
            case "disable":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionDisableUsage", true, null);
                    return;
                }

                handleExpansionStateChange(sender, expansionManager, expansionManager.disableExpansion(args[2]), args[2], msgManager,
                        "Messages.commandExpansionDisableSuccess",
                        "Messages.commandExpansionDisableNotLoaded",
                        "Messages.commandExpansionNotFound",
                        "Messages.commandExpansionDisableFailed");
                break;
            case "reload":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionReloadUsage", true, null);
                    return;
                }

                handleExpansionStateChange(sender, expansionManager, expansionManager.reloadExpansion(args[2]), args[2], msgManager,
                        "Messages.commandExpansionReloadSuccess",
                        null,
                        "Messages.commandExpansionNotFound",
                        "Messages.commandExpansionReloadFailed");
                break;
            case "reloadall":
                int reloaded = expansionManager.reloadAllExpansions();
                Map<String, String> repl = new HashMap<>();
                repl.put("%COUNT%", String.valueOf(reloaded));
                if (reloaded > 0) {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionReloadAllSuccess", true, repl);
                } else {
                    msgManager.sendConfigMessage(sender, "Messages.commandExpansionReloadAllFailed", true, null);
                }
                break;
            default:
                msgManager.sendConfigMessage(sender, "Messages.commandExpansionUsage", true, null);
                break;
        }
    }

    private void handleExpansionStateChange(CommandSender sender,
                                            ExpansionManager expansionManager,
                                            ExpansionManager.ExpansionActionResult result,
                                            String inputName,
                                            MessagesManager msgManager,
                                            String successKey,
                                            String alreadyKey,
                                            String notFoundKey,
                                            String failedKey) {
        ExpansionMetadata metadata = expansionManager == null ? null : expansionManager.getExpansionMetadata(inputName);
        String displayName = metadata != null ? metadata.getName() : inputName;
        Map<String, String> repl = new HashMap<>();
        repl.put("%EXPANSION%", displayName);

        switch (result) {
            case SUCCESS:
                msgManager.sendConfigMessage(sender, successKey, true, repl);
                break;
            case ALREADY_ENABLED:
                if (alreadyKey != null) {
                    msgManager.sendConfigMessage(sender, alreadyKey, true, repl);
                } else {
                    msgManager.sendConfigMessage(sender, successKey, true, repl);
                }
                break;
            case ALREADY_DISABLED:
                if (alreadyKey != null) {
                    msgManager.sendConfigMessage(sender, alreadyKey, true, repl);
                } else {
                    msgManager.sendConfigMessage(sender, failedKey, true, repl);
                }
                break;
            case NOT_FOUND:
                msgManager.sendConfigMessage(sender, notFoundKey, true, repl);
                break;
            default:
                msgManager.sendConfigMessage(sender, failedKey, true, repl);
                break;
        }
    }

    public void set(CommandSender sender, String[] args, MessagesManager msgManager){
        if (args.length < 2) {
            msgManager.sendConfigMessage(sender, "Messages.timerSetError", true, null);
            return;
        }

        String timeHHMMSS = args[1];
        String timerId = (args.length >= 3) ? args[2] : null;

        Timer timer = VCTActions.createTimer(timeHHMMSS, timerId, sender);
        if (timer == null) {
            msgManager.sendConfigMessage(sender, "Messages.timerSetFormatIncorrect", true, null);
            return;
        }

        Map<String, String> repl = new HashMap<>();
        repl.put("%HH%", String.format("%02d", Integer.parseInt(timer.getTimeLeftHH())));
        repl.put("%MM%", String.format("%02d", Integer.parseInt(timer.getTimeLeftMM())));
        repl.put("%SS%", String.format("%02d", Integer.parseInt(timer.getTimeLeftSS())));

        msgManager.sendConfigMessage(sender, "Messages.timerStart", true, repl);
    }

    public void pause(CommandSender sender, MessagesManager msgManager){
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            msgManager.sendConfigMessage(sender, "Messages.timerDontExists", true, null);
            return;
        }
        timer.pause();
        msgManager.sendConfigMessage(sender, "Messages.timerPause", true, null);

        Bukkit.getPluginManager().callEvent(new VCTEvent(timer, VCTEvent.VCTEventType.PAUSE, sender));
    }

    public void resume(CommandSender sender, MessagesManager msgManager){
        Timer timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            msgManager.sendConfigMessage(sender, "Messages.timerDontExists", true, null);
            return;
        }
        timer.resume();
        msgManager.sendConfigMessage(sender, "Messages.timerResume", true, null);

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
            sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix +"&7Modifiers for the timer"));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &eadd &7- Add time to the timer."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &eset &7- Set time to the timer."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &etake &7- Take time to the timer."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &ebossbar_color &7- Change the color of the bossbar."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &ebossbar_style &7- Change the segments style of the bossbar."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &esound &7- Change the sound that plays each time a second is lowered."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &esound_enable &7- Toggle whether the sound should be played or not."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &esound_volume &7- Change the volume of the sound being played."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &esound_pitch &7- Change the pitch of the sound being played."));
            sender.sendMessage(MessagesManager.getColoredMessage("&6> &etext &7- Change the text of the boss bar."));
            return;
        }

        timer = TimerManager.getInstance().getTimer();
        if (timer == null) {
            msgManager.sendConfigMessage(sender, "Messages.timerDontExists", true, null);
            return;
        }

        String modifier = args[1].toLowerCase();
        switch (modifier) {
            case "add":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyAddError", true, null);
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
                    msgManager.sendConfigMessage(sender, "Messages.timerSetFormatIncorrect", true, null);
                    return;
                }

                if (totalSecondsToAdd == 0) {
                    msgManager.sendConfigMessage(sender, "Messages.timerSetFormatOutRange", true, null);
                    return;
                }

                boolean addSuccess = VCTActions.modifyTimer("add", args[2], sender);
                if (!addSuccess) {
                    msgManager.sendConfigMessage(sender, "Messages.timerDontExists", true, null);
                    return;
                }

                Map<String, String> addRepl = new HashMap<>();
                addRepl.put("%HH%", String.format("%02d", addHours));
                addRepl.put("%MM%", String.format("%02d", addMinutes));
                addRepl.put("%SS%", String.format("%02d", addSeconds));

                msgManager.sendConfigMessage(sender, "Messages.timerModifyAdd", true, addRepl);
                return;
            case "set":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyTakeError", true, null);
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
                    msgManager.sendConfigMessage(sender, "Messages.timerSetFormatIncorrect", true, null);
                    return;
                }

                boolean setSuccess = VCTActions.modifyTimer("set", args[2], sender);
                if (!setSuccess) {
                    msgManager.sendConfigMessage(sender, "Messages.timerDontExists", true, null);
                    return;
                }

                Map<String, String> setRepl = new HashMap<>();
                setRepl.put("%HH%", String.format("%02d", setHours));
                setRepl.put("%MM%", String.format("%02d", setMinutes));
                setRepl.put("%SS%", String.format("%02d", setSeconds));

                msgManager.sendConfigMessage(sender, "Messages.timerModifySet", true, setRepl);
                return;
            case "take":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyTakeError", true, null);
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
                    msgManager.sendConfigMessage(sender, "Messages.timerSetFormatIncorrect", true, null);
                    return;
                }

                boolean takeSuccess = VCTActions.modifyTimer("take", args[2], sender);
                if (!takeSuccess) {
                    msgManager.sendConfigMessage(sender, "Messages.timerDontExists", true, null);
                    return;
                }

                Map<String, String> takeRepl = new HashMap<>();
                takeRepl.put("%HH%", String.format("%02d", takeHours));
                takeRepl.put("%MM%", String.format("%02d", takeMinutes));
                takeRepl.put("%SS%", String.format("%02d", takeSeconds));

                msgManager.sendConfigMessage(sender, "Messages.timerModifyTake", true, takeRepl);
                return;
            case "bossbar_color":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyBarcolorError", true, null);
                    return;
                }
                String colorName = args[2].toUpperCase();

                boolean bcSuccess = VCTActions.modifyTimer("bossbar_color", colorName, sender);

                if (bcSuccess) {
                    Map<String, String> barcolorRepl = new HashMap<>();
                    barcolorRepl.put("%TIMER%", timer.getTimerId());
                    barcolorRepl.put("%COLOR%", colorName);

                    msgManager.sendConfigMessage(sender, "Messages.timerModifyBarcolor", true, barcolorRepl);
                } else {
                    Map<String, String> barcolorInvRepl = new HashMap<>();
                    barcolorInvRepl.put("%COLOR%", colorName);

                    msgManager.sendConfigMessage(sender, "Messages.timerModifyBarcolorInvalid", true, barcolorInvRepl);
                }
                return;
            case "bossbar_style":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyBarstyleError", true, null);
                    return;
                }
                String styleName = args[2].toUpperCase();

                boolean bsSuccess = VCTActions.modifyTimer("bossbar_style", styleName, sender);

                if (bsSuccess) {
                    Map<String, String> barstyleRepl = new HashMap<>();
                    barstyleRepl.put("%TIMER%", timer.getTimerId());
                    barstyleRepl.put("%STYLE%", styleName);

                    msgManager.sendConfigMessage(sender, "Messages.timerModifyBarstyle", true, barstyleRepl);
                } else {
                    Map<String, String> barstyleInvRepl = new HashMap<>();
                    barstyleInvRepl.put("%STYLE%", styleName);

                    msgManager.sendConfigMessage(sender, "Messages.timerModifyBarstyleInvalid", true, barstyleInvRepl);
                }
                return;
            case "sound":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundError", true, null);
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
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundRequireQuotes", true, null);
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

                    msgManager.sendConfigMessage(sender, "Messages.timerModifySound", true, soundRepl);
                } else {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundError", true, null);
                }
                return;
            case "sound_enable":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundenableError", true, null);
                    return;
                }

                String value = args[2].toLowerCase();

                boolean seSuccess = VCTActions.modifyTimer("sound_enable", value, sender);

                if (seSuccess) {
                    Map<String, String> soundenableRepl = new HashMap<>();
                    soundenableRepl.put("%TIMER%", timer.getTimerId());
                    soundenableRepl.put("%SOUNDENABLE%", value);

                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundenable", true, soundenableRepl);
                } else {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundenableInvalid", true, null);
                }
                return;
            case "sound_volume":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundvolumeError", true, null);
                    return;
                }

                float newVolume;
                try {
                    newVolume = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundvolumeInvalid", true, null);
                    return;
                }

                if (newVolume < 0.1f || newVolume > 2.0f) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundvolumeOutRange", true, null);
                    return;
                }

                boolean svSuccess = VCTActions.modifyTimer("sound_volume", String.valueOf(newVolume), sender);

                if (svSuccess) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("%TIMER%", timer.getTimerId());
                    repl.put("%VOLUME%", String.valueOf(newVolume));
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundvolume", true, repl);
                }
                return;
            case "sound_pitch":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundpitchError", true, null);
                    return;
                }

                float newPitch;
                try {
                    newPitch = Float.parseFloat(args[2]);
                } catch (NumberFormatException e) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundpitchInvalid", true, null);
                    return;
                }

                if (newPitch < 0.1f || newPitch > 2.0f) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundpitchOutRange", true, null);
                    return;
                }

                boolean spSuccess = VCTActions.modifyTimer("sound_pitch", String.valueOf(newPitch), sender);

                if (spSuccess) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("%TIMER%", timer.getTimerId());
                    repl.put("%PITCH%", String.valueOf(newPitch));
                    msgManager.sendConfigMessage(sender, "Messages.timerModifySoundpitch", true, repl);
                }
                return;
            case "text":
                if (args.length < 3) {
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyTextError", true, null);
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
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyTextRequireQuotes", true, null);
                    return;
                }

                if (rawText.isEmpty()) return;

                boolean textSuccess = VCTActions.modifyTimer("text", rawText, sender);

                if (textSuccess) {
                    Map<String, String> repl = new HashMap<>();
                    repl.put("%TIMER%", timer.getTimerId());
                    repl.put("%TEXT%", rawText);
                    msgManager.sendConfigMessage(sender, "Messages.timerModifyText", true, repl);
                }
                return;
        }
        msgManager.sendConfigMessage(sender, "Messages.timerModifyInvalid", true, null);
    }
    

    public void help(CommandSender sender){
        sender.sendMessage(MessagesManager.getColoredMessage(VoiidCountdownTimer.prefix +"&7Running &dVoiid Countdown Timer &ev"+VoiidCountdownTimer.getInstance().getDescription().getVersion()));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct help &7- Shows this message."));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct reload &e<config|expansion|allexpansions> &7- Reload plugin resources."));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct set &e<HH:MM:SS> &7- Set the timer."));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct pause &7- Pause the timer."));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct resume &7- Resume the timer."));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct stop &7- Stop the timer."));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct modify &e<modifier> &7- Modify the timer."));
        sender.sendMessage(MessagesManager.getColoredMessage("&5> &6/vct expansion &e<info|enable|disable|reload|reloadall> &7- Manage expansions."));

        ExpansionManager expansionManager = VoiidCountdownTimer.getExpansionManager();
        if (expansionManager != null) {
            for (String line : expansionManager.getHelpLines()) {
                sender.sendMessage(MessagesManager.getColoredMessage(line));
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
                }else if(args[0].equalsIgnoreCase("reload")){
                    subcommands.add("config");subcommands.add("expansion");subcommands.add("allexpansions");
                }else if(args[0].equalsIgnoreCase("expansion")) {
                    subcommands.add("info");subcommands.add("enable");
                    subcommands.add("disable");subcommands.add("reload");
                    subcommands.add("reloadall");
                }

                for(String c : subcommands) {
                    if(args[1].isEmpty() || c.startsWith(args[1].toLowerCase())) {
                        subcompletions.add(c);
                    }
                }
                if(!subcompletions.isEmpty()) return subcompletions;
            } else if (args.length == 3){
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
                } else if(args[0].equalsIgnoreCase("reload") && args[1].equalsIgnoreCase("expansion")) {
                    if (expansionManager != null) {
                        List<String> names = expansionManager.getKnownExpansionNames();
                        return filterByPrefix(names, args[2]);
                    }
                } else if(args[0].equalsIgnoreCase("expansion")) {
                    if (expansionManager != null) {
                        if (args[1].equalsIgnoreCase("info") || args[1].equalsIgnoreCase("enable") || args[1].equalsIgnoreCase("reload")) {
                            List<String> names = expansionManager.getKnownExpansionNames();
                            return filterByPrefix(names, args[2]);
                        } else if (args[1].equalsIgnoreCase("disable")) {
                            List<String> names = expansionManager.getLoadedExpansionNames();
                            return filterByPrefix(names, args[2]);
                        }
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

    private List<String> filterByPrefix(List<String> values, String prefix) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        List<String> filtered = new ArrayList<>();
        String lower = prefix == null ? "" : prefix.toLowerCase(Locale.ROOT);
        for (String value : values) {
            if (lower.isEmpty() || value.toLowerCase(Locale.ROOT).startsWith(lower)) {
                filtered.add(value);
            }
        }

        return filtered.isEmpty() ? null : filtered;
    }
}