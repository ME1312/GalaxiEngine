package net.ME1312.Galaxi.Event.Engine;

import net.ME1312.Galaxi.Event.CancellableEvent;
import net.ME1312.Galaxi.Event.ReverseOrder;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;

/**
 * Galaxi Engine Stop Event Class
 */
@ReverseOrder
public class GalaxiStopEvent extends CancellableEvent {
    private final int code;

    /**
     * Galaxi Engine Stop Event
     *
     * @param engine GalaxiEngine
     * @param code Exit Code
     */
    public GalaxiStopEvent(Galaxi engine, int code) {
        if (Util.isNull(engine)) throw new NullPointerException();
        this.code = code;
    }

    /**
     * Exit Code from the app
     *
     * @return Exit Code
     */
    public int getStopCode() {
        return code;
    }
}
