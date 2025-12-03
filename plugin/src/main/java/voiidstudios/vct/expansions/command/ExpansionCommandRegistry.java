package voiidstudios.vct.expansions.command;

import org.bukkit.command.CommandSender;

import voiidstudios.vct.expansions.ScriptExpansion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ExpansionCommandRegistry {
    private final Map<String, ExpansionCommand> commandsByAlias = new LinkedHashMap<>();
    private final Map<ScriptExpansion, List<ExpansionCommand>> commandsByExpansion = new LinkedHashMap<>();
    private final List<ExpansionCommand> orderedCommands = new ArrayList<>();

    public synchronized boolean registerCommand(ScriptExpansion expansion, ExpansionCommand command) {
        boolean registered = false;
        List<String> identifiers = command.getAllIdentifiers();

        for (String identifier : identifiers) {
            if (identifier == null || identifier.isEmpty()) {
                continue;
            }

            String key = identifier.toLowerCase(Locale.ROOT);
            if (commandsByAlias.containsKey(key)) {
                expansion.getPlugin().getLogger().warning(String.format(Locale.ROOT,
                        "Unable to register expansion command '%s' from %s because the identifier '%s' is already in use.",
                        command.getName(), expansion.getMetadata().getName(), identifier));
                continue;
            }

            commandsByAlias.put(key, command);
            registered = true;
        }

        if (registered) {
            orderedCommands.add(command);
            commandsByExpansion.computeIfAbsent(expansion, key -> new ArrayList<>()).add(command);
        }

        return registered;
    }

    public synchronized void unregisterCommands(ScriptExpansion expansion) {
        List<ExpansionCommand> commands = commandsByExpansion.remove(expansion);
        if (commands == null) {
            return;
        }

        for (ExpansionCommand command : commands) {
            orderedCommands.remove(command);
            commandsByAlias.entrySet().removeIf(entry -> entry.getValue() == command);
        }
    }

    public synchronized boolean execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return false;
        }

        ExpansionCommand command = findCommand(args[0]);
        if (command == null) {
            return false;
        }

        String[] trimmedArgs = new String[Math.max(0, args.length - 1)];
        if (trimmedArgs.length > 0) {
            System.arraycopy(args, 1, trimmedArgs, 0, trimmedArgs.length);
        }

        return command.execute(sender, trimmedArgs);
    }

    public synchronized List<String> tabComplete(CommandSender sender, String[] args) {
        if (args.length == 0) {
            return Collections.emptyList();
        }

        ExpansionCommand command = findCommand(args[0]);
        if (command == null) {
            return null;
        }

        String[] trimmedArgs = new String[Math.max(0, args.length - 1)];
        if (trimmedArgs.length > 0) {
            System.arraycopy(args, 1, trimmedArgs, 0, trimmedArgs.length);
        }

        return command.tabComplete(sender, trimmedArgs);
    }

    public synchronized List<String> getRootSuggestions(String partial) {
        String lowercase = partial == null ? "" : partial.toLowerCase(Locale.ROOT);
        Set<String> suggestions = new LinkedHashSet<>();

        for (ExpansionCommand command : orderedCommands) {
            for (String identifier : command.getAllIdentifiers()) {
                if (identifier == null) {
                    continue;
                }

                String lower = identifier.toLowerCase(Locale.ROOT);
                if (lowercase.isEmpty() || lower.startsWith(lowercase)) {
                    suggestions.add(identifier);
                }
            }
        }

        return new ArrayList<>(suggestions);
    }

    public synchronized List<String> getHelpLines() {
        List<String> lines = new ArrayList<>();

        for (ExpansionCommand command : orderedCommands) {
            String description = command.getDescription();
            String helpLine = String.format(Locale.ROOT,
                    "&5> &6/vct %s &7- %s &8[%s]",
                    command.getName(),
                    description == null || description.isEmpty() ? "Comando proporcionado por una expansión" : description,
                    command.getExpansion().getMetadata().getName());
            lines.add(helpLine);
        }

        return lines;
    }

    private ExpansionCommand findCommand(String identifier) {
        if (identifier == null) {
            return null;
        }

        return commandsByAlias.get(identifier.toLowerCase(Locale.ROOT));
    }

    public synchronized Collection<ExpansionCommand> getRegisteredCommands() {
        return Collections.unmodifiableList(new ArrayList<>(orderedCommands));
    }
}