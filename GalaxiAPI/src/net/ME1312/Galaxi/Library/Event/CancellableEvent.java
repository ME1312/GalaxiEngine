package net.ME1312.Galaxi.Library.Event;

/**
 * Cancellable SubEvent Layout Class
 */
public abstract class CancellableEvent extends Event {
    private boolean cancelled;

    /**
     * Gets if the Event has been Cancelled
     *
     * @return Cancelled Status
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets if the Event is Cancelled
     *
     * @param value
     */
    public void setCancelled(boolean value) {
        this.cancelled = value;
    }
}
