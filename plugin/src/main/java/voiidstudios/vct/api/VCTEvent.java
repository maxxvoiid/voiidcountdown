package voiidstudios.vct.api;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.Nullable;

public class VCTEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public enum VCTEventType {
        CREATE,
        CHANGE,
        FINISH,
        PAUSE,
        RESUME,
        MODIFY,
        STOP
    }

    private final Timer timer;
    private final VCTEventType type;
    private final CommandSender sender;
    private final @Nullable String modifier;
    private final @Nullable String modification;

    public VCTEvent(@Nullable Timer timer, VCTEventType type, @Nullable CommandSender sender) {
        this.timer = timer;
        this.type = type;
        this.sender = sender;
        this.modifier = null;
        this.modification = null;
    }

    public VCTEvent(@Nullable Timer timer, VCTEventType type, @Nullable CommandSender sender, @Nullable String modifier) {
        this.timer = timer;
        this.type = type;
        this.sender = sender;
        this.modifier = modifier;
        this.modification = null;
    }

    public VCTEvent(@Nullable Timer timer, VCTEventType type, @Nullable CommandSender sender, @Nullable String modifier, @Nullable String modification) {
        this.timer = timer;
        this.type = type;
        this.sender = sender;
        this.modifier = modifier;
        this.modification = modification;
    }


    @Nullable
    public Timer getTimer() {
        return timer;
    }

    @Nullable
    public VCTEventType getType() {
        return type;
    }

    @Nullable
    public CommandSender getSender() {
        return sender;
    }

    @Nullable
    public Player getPlayer() {
        return (sender instanceof Player) ? (Player) sender : null;
    }

    @Nullable
    public String getTimerId() {
        return timer != null ? timer.getTimerId() : null;
    }

    @Nullable
    public String getInitialTime() {
        return timer != null ? timer.getInitialTime() : null;
    }

    @Nullable
    public String getTimeLeft() {
        return timer != null ? timer.getTimeLeft() : null;
    }

    @Nullable
    public String getModifier() {
        return modifier;
    }

    @Nullable
    public String getModification() {
        return modification;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}