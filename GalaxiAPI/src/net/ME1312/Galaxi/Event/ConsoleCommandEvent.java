package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Event.Cancellable;
import net.ME1312.Galaxi.Library.Event.Event;
import net.ME1312.Galaxi.Library.Util;

/**
 * Console Command Event Class
 */
public class ConsoleCommandEvent extends Event implements Cancellable {
    private boolean cancelled;
    private String command;

    /**
     * Console Command Event
     *
     * @param engine GalaxiEngine
     * @param command Command
     */
    public ConsoleCommandEvent(Galaxi engine, String command) {
        if (Util.isNull(engine, command)) throw new NullPointerException();
        this.command = command;
    }

    /**
     * Get the Command
     *
     * @return Command
     */
    public String getCommand() {
        return command;
    }

    /**
     * Set the Command
     *
     * @param value Command
     */
    public void setCommand(String value) {
        this.command = value;
    }


    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean value) {
        this.cancelled = value;
    }
}
