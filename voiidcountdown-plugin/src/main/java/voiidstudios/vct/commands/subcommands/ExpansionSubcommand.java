package voiidstudios.vct.commands.subcommands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.commands.VCTSubcommand;
import voiidstudios.vct.expansions.ExpansionManager;
import voiidstudios.vct.expansions.ExpansionMetadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpansionSubcommand implements VCTSubcommand {
    private final SubcommandContext context;

    public ExpansionSubcommand(SubcommandContext context) { this.context = context; }
    public String name() { return "expansion"; }
    public String permission() { return "voiidcountdowntimer.command.expansion"; }

    public boolean execute(CommandSender sender, String[] args) {
        ExpansionManager expansionManager = context.getExpansionManager();
        if (expansionManager == null) {
            context.getMessagesManager().send(sender, "expansion.disabled");
            return true;
        }

        if (args.length < 2) {
            context.getMessagesManager().sendSection(sender, "expansion.help", true, null);
            return true;
        }

        String action = args[1].toLowerCase(Locale.ROOT);
        String targetName = args.length >= 3 ? args[2] : null;

        switch (action) {
            case "info":
                if (targetName == null) { context.getMessagesManager().send(sender, "expansion.no_id_specified"); return true; }
                ExpansionMetadata infoMetadata = expansionManager.getExpansionMetadata(targetName);
                if (infoMetadata == null) {
                    Map<String, String> repl = new HashMap<String, String>(); repl.put("%EXPANSION%", targetName);
                    context.getMessagesManager().send(sender, "expansion.not_found", repl); return true;
                }
                boolean infoLoaded = expansionManager.isExpansionLoaded(infoMetadata.getName());
                List<String> authors = infoMetadata.getAuthors();
                String authorsText = authors.isEmpty() ? "N/A" : String.join("&7, &f", authors);
                String description = infoMetadata.getDescription();
                if (description == null || description.trim().isEmpty()) { description = "No description."; }
                Map<String, String> infoRepl = new HashMap<String, String>();
                infoRepl.put("%EXPANSION%", infoMetadata.getName());
                infoRepl.put("%STATUS%", infoLoaded ? "Enabled" : "Disabled");
                infoRepl.put("%STATUSCOLOR%", infoLoaded ? "&a" : "&c");
                infoRepl.put("%AUTHORS%", authorsText);
                infoRepl.put("%VERSION%", infoMetadata.getVersion());
                infoRepl.put("%DESCRIPTION%", description);
                context.getMessagesManager().sendSection(sender, "expansion.info", true, infoRepl);
                return true;
            case "enable":
            case "disable":
            case "reload":
                return handleToggleAction(sender, expansionManager, action, targetName);
            case "reloadall":
                int reloaded = expansionManager.reloadAllExpansions();
                if (reloaded == 0) {
                    context.getMessagesManager().send(sender, "expansion.reloadall.error");
                } else {
                    Map<String, String> repl = new HashMap<String, String>(); repl.put("%EXPANSIONS%", String.valueOf(reloaded));
                    context.getMessagesManager().send(sender, "expansion.reloadall.success", repl);
                }
                return true;
            case "list":
                sendList(sender, expansionManager);
                return true;
            default:
                context.getMessagesManager().send(sender, "expansion.reloadall.invalid");
                return true;
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        ExpansionManager expansionManager = context.getExpansionManager();
        if (args.length == 2) {
            List<String> sub = new ArrayList<String>();
            sub.add("info"); sub.add("enable"); sub.add("disable"); sub.add("reload"); sub.add("reloadall"); sub.add("list");
            return filter(sub, args[1]);
        }
        if (args.length == 3 && expansionManager != null && !"reloadall".equalsIgnoreCase(args[1])) {
            List<String> names = expansionManager.getKnownExpansionNames();
            List<String> matches = new ArrayList<String>();
            for (String name : names) {
                if (args[2].isEmpty() || name.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))) {
                    matches.add(name);
                }
            }
            return matches.isEmpty() ? null : matches;
        }
        return null;
    }

    private boolean handleToggleAction(CommandSender sender, ExpansionManager expansionManager, String action, String targetName) {
        if (targetName == null) { context.getMessagesManager().send(sender, "expansion.no_id_specified"); return true; }
        ExpansionMetadata metadata = expansionManager.getExpansionMetadata(targetName);
        if (metadata == null) {
            Map<String, String> repl = new HashMap<String, String>(); repl.put("%EXPANSION%", targetName);
            context.getMessagesManager().send(sender, "expansion.not_found", repl); return true;
        }

        if ("enable".equals(action) && expansionManager.isExpansionLoaded(metadata.getName())) {
            Map<String, String> repl = new HashMap<String, String>(); repl.put("%EXPANSION%", targetName);
            context.getMessagesManager().send(sender, "expansion.enable.already_enabled", repl); return true;
        }
        if ("disable".equals(action) && !expansionManager.isExpansionLoaded(metadata.getName())) {
            Map<String, String> repl = new HashMap<String, String>(); repl.put("%EXPANSION%", targetName);
            context.getMessagesManager().send(sender, "expansion.disable.already_disabled", repl); return true;
        }
        if ("reload".equals(action) && !expansionManager.isExpansionLoaded(metadata.getName())) {
            Map<String, String> repl = new HashMap<String, String>(); repl.put("%EXPANSION%", targetName);
            context.getMessagesManager().send(sender, "expansion.reload.disabled", repl); return true;
        }

        boolean success;
        if ("enable".equals(action)) success = expansionManager.enableExpansion(metadata.getName());
        else if ("disable".equals(action)) success = expansionManager.disableExpansion(metadata.getName());
        else success = expansionManager.reloadExpansion(metadata.getName());

        Map<String, String> repl = new HashMap<String, String>(); repl.put("%EXPANSION%", targetName);
        context.getMessagesManager().send(sender, success ? "expansion." + action + ".success" : "expansion." + action + ".error", repl);
        return true;
    }

    private void sendList(CommandSender sender, ExpansionManager expansionManager) {
        List<String> known = expansionManager.getKnownExpansionNames();
        Map<String, String> titleRepl = new HashMap<String, String>();
        titleRepl.put("%EXPANSIONS%", String.valueOf(known.size()));
        String hoverText = context.getMessagesManager().getListAsSingleString("expansion.list.hint", null);

        TextComponent infoIcon = new TextComponent("ℹ ");
        infoIcon.setColor(ChatColor.AQUA);
        infoIcon.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hoverText).create()));
        infoIcon.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://vctdocs.mintlify.app/usage-guide/expansions"));

        String prefixColored = ChatColor.translateAlternateColorCodes('&', VoiidCountdownTimer.prefix);
        TextComponent message = new TextComponent(prefixColored);
        message.addExtra(infoIcon);
        String titleColored = ChatColor.translateAlternateColorCodes('&', context.getMessagesManager().getTranslated("expansion.list.title", titleRepl, false));
        message.addExtra(new TextComponent(titleColored));
        sender.spigot().sendMessage(message);

        if (known.isEmpty()) {
            context.getMessagesManager().send(sender, "expansion.list.no_extensions");
            return;
        }

        TextComponent listComponent = new TextComponent("");
        boolean first = true;
        for (String name : known) {
            boolean active = expansionManager.isExpansionLoaded(name);
            ChatColor color = active ? ChatColor.GREEN : ChatColor.RED;
            String status = active ? context.getMessagesManager().getTranslated("expansion.list.enabled", null, false) : context.getMessagesManager().getTranslated("expansion.list.disabled", null, false);

            TextComponent expansionComponent = new TextComponent((first ? "" : ChatColor.GRAY + ", ") + color + name);
            expansionComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vct expansion info " + name));
            expansionComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(status).create()));

            listComponent.addExtra(expansionComponent);
            first = false;
        }

        sender.spigot().sendMessage(listComponent);
    }

    private List<String> filter(List<String> source, String startsWith) {
        List<String> output = new ArrayList<String>();
        for (String candidate : source) {
            if (startsWith.isEmpty() || candidate.startsWith(startsWith.toLowerCase(Locale.ROOT))) {
                output.add(candidate);
            }
        }
        return output.isEmpty() ? null : output;
    }
}
