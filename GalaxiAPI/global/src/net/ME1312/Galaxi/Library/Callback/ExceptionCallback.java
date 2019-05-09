package net.ME1312.Galaxi.Library.Callback;

/**
 * Exception Callback
 *
 * @param <T> Object
 */
public interface ExceptionCallback<T> {
    /**
     * Callback
     *
     * @param obj Object
     * @throws Throwable Exception
     */
    void run(T obj) throws Throwable;
}
