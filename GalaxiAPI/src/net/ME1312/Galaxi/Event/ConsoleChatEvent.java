package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Event.CancellableEvent;
import net.ME1312.Galaxi.Library.Event.Event;
import net.ME1312.Galaxi.Library.Util;

/**
 * Console Chat Event Class
 */
public class ConsoleChatEvent extends CancellableEvent {
    private String chat;

    /**
     * Console Chat Event
     *
     * @param engine GalaxiEngine
     * @param chat Chat Message
     */
    public ConsoleChatEvent(Galaxi engine, String chat) {
        if (Util.isNull(engine, chat)) throw new NullPointerException();
        this.chat = chat;
    }

    /**
     * Get the Chat Message
     *
     * @return Chat Message
     */
    public String getMessage() {
        return chat;
    }

    /**
     * Set the Chat Message
     *
     * @param value Chat Message
     */
    public void setMessage(String value) {
        this.chat = value;
    }
}
