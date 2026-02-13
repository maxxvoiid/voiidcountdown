package voiidstudios.vct.commands.subcommands;

import voiidstudios.vct.commands.MainCommand;
import voiidstudios.vct.expansions.ExpansionManager;
import voiidstudios.vct.managers.MessagesManager;

public class SubcommandContext {
    private final MainCommand mainCommand;

    public SubcommandContext(MainCommand mainCommand) {
        this.mainCommand = mainCommand;
    }

    public MainCommand getMainCommand() {
        return mainCommand;
    }

    public MessagesManager getMessagesManager() {
        return mainCommand.getMessagesManager();
    }

    public ExpansionManager getExpansionManager() {
        return mainCommand.getExpansionManager();
    }
}
