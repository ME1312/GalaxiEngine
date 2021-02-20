package net.ME1312.Galaxi.Event.Engine;

import net.ME1312.Galaxi.Event.Event;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;

/**
 * Galaxi Engine Reload Event Class
 */
public class GalaxiReloadEvent extends Event {
    /**
     * Galaxi Engine Reload Event
     *
     * @param engine GalaxiEngine
     */
    public GalaxiReloadEvent(Galaxi engine) {
        if (Util.isNull(engine)) throw new NullPointerException();
    }
}