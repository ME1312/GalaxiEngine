package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Event.CancellableEvent;
import net.ME1312.Galaxi.Library.Util;

/**
 * Non-Specific Console Input Event
 *
 * @see CommandEvent Command-specific Event
 * @see ConsoleChatEvent Chat-specific Event
 */
public class ConsoleInputEvent extends CancellableEvent {
    private String input;

    /**
     * Console Command Event
     *
     * @param engine GalaxiEngine
     * @param input Input
     */
    public ConsoleInputEvent(Galaxi engine, String input) {
        if (Util.isNull(engine, input)) throw new NullPointerException();
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
