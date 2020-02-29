package net.ME1312.Galaxi.Library;

/**
 * Asynchronous Task Consolidator Class
 */
public class AsyncConsolidator {
    private final Runnable action;
    private int tasks = 0;
    private boolean done;

    /**
     * Create a Asynchronous Task Consolidator
     *
     * @param action Resulting Action
     */
    public AsyncConsolidator(Runnable action) {
        this.action = action;
    }

    /**
     * Call this before running each task
     */
    public void reserve() {
        ++tasks;
    }

    /**
     * Call this after running each task
     */
    public void release() {
        if (tasks > 0) --tasks;
        if (!done && tasks <= 0) {
            done = true;
            action.run();
        }
    }
}
