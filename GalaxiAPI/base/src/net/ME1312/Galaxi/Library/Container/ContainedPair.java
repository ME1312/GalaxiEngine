package net.ME1312.Galaxi.Library.Container;

/**
 * Key-Value Pair Container Class
 *
 * @param <K> Key Type
 * @param <V> Value Type
 */
public class ContainedPair<K, V> extends Pair<K, V> {
    public K key;
    public V value;

    /**
     * Creates the Container
     */
    public ContainedPair() {}

    /**
     * Creates the Container
     *
     * @param key Key
     * @param value Value
     */
    public ContainedPair(K key, V value) {
        this.key = key;
        this.value = value;
    }


    @Override
    public K key() {
        return this.key;
    }

    @Override
    public K key(K key) {
        return this.key = key;
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
