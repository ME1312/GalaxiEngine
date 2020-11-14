package net.ME1312.Galaxi.Library.Container;

/**
 * Value Container Class
 *
 * @param <V> Value Type
 */
public class Container<V> extends Value<V> {
    public V value;

    /**
     * Creates the Container
     */
    public Container() {}

    /**
     * Creates the Container
     *
     * @param value Value
     */
    public Container(V value) {
        this.value = value;
    }

    @Override
    public V value() {
        return this.value;
    }

    @Override
    public V value(V value) {
        return this.value = value;
    }
}
