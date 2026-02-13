package voiidstudios.vct.commands;

import org.bukkit.command.CommandSender;

import java.util.List;

public interface VCTSubcommand {
    String name();

    String permission();

    boolean execute(CommandSender sender, String[] args);

    List<String> tabComplete(CommandSender sender, String[] args);
}
