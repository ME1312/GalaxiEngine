package net.ME1312.Galaxi.Library.Container;

/**
 * Primitive Container Class
 *
 * @param <V> Item
 */
public class PrimitiveContainer<V> {
    public V value;

    /**
     * Creates a Container
     *
     * @param item Object to Store
     */
    public PrimitiveContainer(V item) {
        value = item;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof PrimitiveContainer) {
            if (value == null || ((PrimitiveContainer) object).value == null) {
                return value == ((PrimitiveContainer) object).value;
            } else {
                return value.equals(((PrimitiveContainer) object).value);
            }
        } else {
            return super.equals(object);
        }
    }
}
