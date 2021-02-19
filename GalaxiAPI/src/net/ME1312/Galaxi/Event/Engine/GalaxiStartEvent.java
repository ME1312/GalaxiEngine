package net.ME1312.Galaxi.Event.Engine;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Event.Event;
import net.ME1312.Galaxi.Library.Util;

/**
 * Galaxi Engine Start Event Class
 */
public class GalaxiStartEvent extends Event {
    /**
     * Galaxi Engine Start Event
     *
     * @param engine GalaxiEngine
     */
    public GalaxiStartEvent(Galaxi engine) {
        if (Util.isNull(engine)) throw new NullPointerException();
    }
}
