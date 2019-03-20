package net.ME1312.Galaxi.Library.Callback;

/**
 * Callback with a return parameter
 *
 * @param <T> Object
 * @param <R> Return parameter
 */
public interface ReturnCallback<T, R> {
    /**
     * Callback
     *
     * @param obj Object
     * @return Return parameter
     */
    R run(T obj);
}
