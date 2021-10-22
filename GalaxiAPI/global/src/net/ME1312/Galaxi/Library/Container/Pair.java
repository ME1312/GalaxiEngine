package net.ME1312.Galaxi.Library.Container;

/**
 * Key-Value Pair Container
 *
 * @param <K> Key Type
 * @param <V> Value Type
 */
public abstract class Pair<K, V> extends Value<V> {

    /**
     * Get the Key
     *
     * @return Key
     */
    public abstract K key();

    /**
     * Set the Key
     *
     * @param key Key
     */
    public abstract K key(K key);

    @Override
    public String toString() {
        return '[' + String.valueOf(key()) + ", " + super.toString() + ')';
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object object) {
        if (object instanceof Pair) {
            if (key() == null || ((Pair) object).key() == null) {
                return key() == ((Pair) object).key() && super.equals(object);
            } else {
                return key().equals(((Pair) object).key()) && super.equals(object);
            }
        } else {
            return false;
        }
    }
}