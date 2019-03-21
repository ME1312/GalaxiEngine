package net.ME1312.Galaxi.Library.Callback;

/**
 * Runnable with a return parameter
 * @param <R> Return parameter
 */
public interface ReturnRunnable<R> {
    /**
     * Runnable
     *
     * @return Return parameter
     */
    R run();
}
