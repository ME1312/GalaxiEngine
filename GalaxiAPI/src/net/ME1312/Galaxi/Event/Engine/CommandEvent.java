package net.ME1312.Galaxi.Event.Engine;

import net.ME1312.Galaxi.Command.CommandSender;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Event.CancellableEvent;
import net.ME1312.Galaxi.Library.Util;

/**
 * Command Event Class
 */
public class CommandEvent extends CancellableEvent {
    private final CommandSender sender;
    private final String raw;
    private String label;
    private String[] args;

    /**
     * Console Command Event
     *
     * @param engine GalaxiEngine
     * @param sender Command Sender
     * @param raw Raw Command
     * @param label Command Label
     * @param args Command Arguments
     */
    public CommandEvent(Galaxi engine, CommandSender sender, String raw, String label, String... args) {
        if (Util.isNull(engine, sender, raw, label, args)) throw new NullPointerException();
        this.sender = sender;
        this.raw = raw;
        this.label = label;
        this.args = args;
    }

    /**
     * Gets who sent this Command
     *
     * @return Command Sender
     */
    public CommandSender getSender() {
        return sender;
    }

    /**
     * Get the Raw Command String
     *
     * @return Raw Command String
     */
    public String getRawCommand() {
        return raw;
    }

    /**
     * Get the Command Label
     *
     * @return Command Label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Set the Command Label
     *
     * @param value Command Label
     */
    public void setLabel(String value) {
        this.label = label;
    }

    /**
     * Get the Command Arguments
     *
     * @return Command Arguments
     */
    public String[] getArguments() {
        return args;
    }

    /**
     * Set the Command Arguments
     *
     * @param value Command Arguments
     */
    public void setArguments(String... value) {
        this.args = value;
    }
}
