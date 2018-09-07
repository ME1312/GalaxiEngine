package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Event.Cancellable;
import net.ME1312.Galaxi.Library.Event.Event;
import net.ME1312.Galaxi.Library.Util;

/**
 * Galaxi Engine Stop Event Class
 */
public class GalaxiStopEvent extends Event implements Cancellable {
    private boolean cancelled;

    /**
     * Galaxi Engine Stop Event
     *
     * @param engine GalaxiEngine
     */
    public GalaxiStopEvent(Galaxi engine) {
        if (Util.isNull(engine)) throw new NullPointerException();
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
