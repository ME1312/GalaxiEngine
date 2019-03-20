package net.ME1312.Galaxi.Library.Callback;

/**
 * Exception Runnable with a return parameter
 *
 * @param <R> Return parameter
 */
public interface ExceptionReturnRunnable<R> {
    /**
     * Runnable
     *
     * @return Return parameter
     * @throws Throwable Exception
     */
    R run() throws Throwable;
}
