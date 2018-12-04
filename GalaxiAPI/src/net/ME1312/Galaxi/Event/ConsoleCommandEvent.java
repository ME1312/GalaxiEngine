package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Event.CancellableEvent;
import net.ME1312.Galaxi.Library.Event.Event;
import net.ME1312.Galaxi.Library.Util;

/**
 * Console Command Event Class
 */
public class ConsoleCommandEvent extends CancellableEvent {
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
}
