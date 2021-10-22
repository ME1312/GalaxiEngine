package net.ME1312.Galaxi.Library.Container;

/**
 * Value Container
 *
 * @param <T> Value Type
 */
public abstract class Value<T> {

    /**
     * Get the Value
     *
     * @return Value
     */
    public abstract T value();

    /**
     * Set the Value
     *
     * @param value Value
     */
    public abstract T value(T value);

    @Override
    public String toString() {
        return String.valueOf(value());
    }

    @SuppressWarnings("rawtypes")
    @Override
    public boolean equals(Object object) {
        if (object instanceof Value) {
            if (value() == null || ((Value) object).value() == null) {
                return value() == ((Value) object).value();
            } else {
                return value().equals(((Value) object).value());
            }
        } else {
            return false;
        }
    }
}
