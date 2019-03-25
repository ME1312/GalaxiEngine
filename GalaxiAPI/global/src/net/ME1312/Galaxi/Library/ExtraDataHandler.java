package net.ME1312.Galaxi.Library;

import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Map.ObjectMapValue;

/**
 * Extra Data Handler Layout Class
 *
 * @param <K> Key Type
 */
public interface ExtraDataHandler<K> {
    /**
     * Add an extra value to this Object
     *
     * @param handle Handle
     * @param value Value
     */
    void addExtra(String handle, Object value);

    /**
     * Determine if an extra value exists
     *
     * @param handle Handle
     * @return Value Status
     */
    boolean hasExtra(String handle);

    /**
     * Get an extra value
     *
     * @param handle Handle
     * @return Value
     */
    ObjectMapValue<K> getExtra(String handle);

    /**
     * Get the extra value section
     *
     * @return Extra Value Section
     */
    ObjectMap<K> getExtra();

    /**
     * Remove an extra value from this Object
     *
     * @param handle Handle
     */
    void removeExtra(String handle);
}
