package net.ME1312.Galaxi.Event.Engine;

import net.ME1312.Galaxi.Event.CancellableEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;

/**
 * Non-Specific Console Input Event
 *
 * @see CommandEvent Command-specific Event
 */
public class ConsoleInputEvent extends CancellableEvent {
    private final String input;

    /**
     * Console Command Event
     *
     * @param engine GalaxiEngine
     * @param input Input
     */
    public ConsoleInputEvent(Galaxi engine, String input) {
        Util.nullpo(engine, input);
        this.input = input;
    }

    /**
     * Get the Input
     *
     * @return Command
     */
    public String getInput() {
        return input;
    }
}
