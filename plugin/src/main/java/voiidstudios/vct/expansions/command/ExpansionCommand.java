package voiidstudios.vct.expansions.command;

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror;
import org.bukkit.command.CommandSender;

import voiidstudios.vct.VoiidCountdownTimer;
import voiidstudios.vct.expansions.ScriptExpansion;
import voiidstudios.vct.managers.MessagesManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

public class ExpansionCommand {
    private final ScriptExpansion expansion;
    private final String name;
    private final List<String> aliases;
    private final String description;
    private final String usage;
    private final String permission;
    private final ScriptObjectMirror executor;
    private final ScriptObjectMirror tabCompleter;

    public ExpansionCommand(ScriptExpansion expansion,
                             String name,
                             List<String> aliases,
                             String description,
                             String usage,
                             String permission,
                             ScriptObjectMirror executor,
                             ScriptObjectMirror tabCompleter) {
        this.expansion = expansion;
        this.name = name;
        this.aliases = aliases == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(aliases));
        this.description = description == null ? "" : description;
        this.usage = usage == null ? "" : usage;
        this.permission = permission == null ? "" : permission;
        this.executor = executor;
        this.tabCompleter = tabCompleter;
    }

    public String getName() {
        return name;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public ScriptExpansion getExpansion() {
        return expansion;
    }

    public String getDescription() {
        return description;
    }

    public String getUsage() {
        return usage;
    }

    public List<String> getAllIdentifiers() {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(name);
        identifiers.addAll(aliases);
        return identifiers;
    }

    public boolean execute(CommandSender sender, String[] args) {
        MessagesManager msgManager = VoiidCountdownTimer.getMessagesManager();

        if (executor == null) {
            return false;
        }

        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            msgManager.send(sender, "command.no_permissions");
            return true;
        }

        try {
            Object result = expansion.callFunction(executor, sender, args);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
            return true;
        } catch (Throwable throwable) {
            expansion.getPlugin().getLogger().log(Level.SEVERE,
                    String.format(Locale.ROOT, "Failed to execute expansion command '%s' from %s", name, expansion.getMetadata().getName()),
                    throwable);
            msgManager.send(sender, "expansion.expansion_command.error");
            return true;
        }
    }

    public List<String> tabComplete(CommandSender sender, String[] args) {
        if (tabCompleter == null) {
            return Collections.emptyList();
        }

        if (permission != null && !permission.isEmpty() && !sender.hasPermission(permission)) {
            return Collections.emptyList();
        }

        try {
            Object result = expansion.callFunction(tabCompleter, sender, args);
            return expansion.convertToStringList(result);
        } catch (Throwable throwable) {
            expansion.getPlugin().getLogger().log(Level.SEVERE,
                    String.format(Locale.ROOT, "Failed to tab-complete expansion command '%s' from %s", name, expansion.getMetadata().getName()),
                    throwable);
        }

        return Collections.emptyList();
    }
}