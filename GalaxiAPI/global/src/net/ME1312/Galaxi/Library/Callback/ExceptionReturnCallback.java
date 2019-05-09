package net.ME1312.Galaxi.Library.Callback;

/**
 * Exception Callback with a return parameter
 *
 * @param <T> Object
 * @param <R> Return parameter
 */
public interface ExceptionReturnCallback<T, R> {
    /**
     * Callback
     *
     * @param obj Object
     * @return Return parameter
     * @throws Throwable Exception
     */
    R run(T obj) throws Throwable;
}
