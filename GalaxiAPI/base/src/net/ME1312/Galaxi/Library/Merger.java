package net.ME1312.Galaxi.Library;

/**
 * Thread Merger Class
 */
public class Merger {
    private final Runnable action;
    private volatile int tasks = 0;

    /**
     * Create a Thread Consolidator
     *
     * @param action Resulting Action (this is run in the last existing thread upon completion)
     */
    public Merger(Runnable action) {
        this.action = action;
    }

    /**
     * Create a Thread Consolidator
     *
     * @param thread Resulting Thread (this is started upon completion)
     */
    public Merger(Thread thread) {
        this.action = thread::start;
    }

    /**
     * See how many threads are still active
     *
     * @return Reservation count
     */
    public int reserved() {
        return tasks;
    }

    /**
     * Call this before starting a thread
     */
    public synchronized void reserve() {
        ++tasks;
    }

    /**
     * Call this before starting multiple threads
     *
     * @param amount Amount of threads
     */
    public synchronized void reserve(int amount) {
        tasks += amount;
    }

    /**
     * Call this at the end of each thread
     */
    public synchronized void release() {
        if (--tasks == 0) action.run();
    }
}
