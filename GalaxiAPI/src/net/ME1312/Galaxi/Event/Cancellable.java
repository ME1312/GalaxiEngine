package net.ME1312.Galaxi.Event;

/**
 * Cancellable SubEvent Layout Class
 */
public interface Cancellable {

    /**
     * Determine if the Event has been Cancelled
     *
     * @return Cancel Status
     */
    default boolean isCancelled() {
        return ((Event) this).cancel;
    }

    /**
     * Sets if the Event is Cancelled
     *
     * @param value Cancel Status
     */
    default void setCancelled(boolean value) {
        ((Event) this).cancel = value;
    }
}
