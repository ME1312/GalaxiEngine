package net.ME1312.Galaxi.Library.Map;

import net.ME1312.Galaxi.Library.Try;
import net.ME1312.Galaxi.Library.Version.Version;

import java.util.*;

/**
 * Object Map Class
 *
 * @param <K> Key Type
 */
@SuppressWarnings({"unchecked", "unused"})
public class ObjectMapValue<K> {
    Object obj;
    K label;
    ObjectMap<K> up;

    protected ObjectMapValue(Object obj) {
        this.obj = obj;
    }

    /**
     * Get the Object Map this Object was defined in
     *
     * @return Object Map
     */
    public ObjectMap<K> getParent() {
        return up;
    }

    /**
     * Get the Handle this Object uses
     *
     * @return Object Handle
     */
    public K getHandle() {
        return label;
    }

    /**
     * Get List
     *
     * @return List
     */
    public List<ObjectMapValue<K>> asList() {
        if (obj != null) {
            List<ObjectMapValue<K>> values = new LinkedList<ObjectMapValue<K>>();
            for (Object value : (List<?>) obj) {
                values.add(up.wrap(null, value));
            }
            return values;
        } else return null;
    }

    /**
     * Get Object
     *
     * @return Object
     */
    public Object asObject() {
        return up.simplify(obj);
    }

    /**
     * Get Object as Object List
     *
     * @return List
     */
    public List<?> asObjectList() {
        return (List<?>) up.simplify(obj);
    }

    /**
     * Get Object as Boolean
     *
     * @return Boolean
     */
    public Boolean asBoolean() {
        return (Boolean) obj;
    }

    /**
     * Get Object as Boolean List
     *
     * @return List
     */
    public List<Boolean> asBooleanList() {
        return (List<Boolean>) obj;
    }

    /**
     * Get Object as Object Map
     *
     * @return Object Map
     */
    public ObjectMap<K> asMap() {
        return (ObjectMap<K>) obj;
    }

    /**
     * Get Object as Object Map List
     *
     * @return Object Map List
     */
    public List<ObjectMap<K>> asMapList() {
        return (List<ObjectMap<K>>) obj;
    }

    /**
     * Get Object as Double
     *
     * @return Double
     */
    public Double asDouble() {
        return ((Number) obj).doubleValue();
    }

    /**
     * Get Object as Double List
     *
     * @return Double List
     */
    public List<Double> asDoubleList() {
        return (List<Double>) obj;
    }

    /**
     * Get Object as Float
     *
     * @return Float
     */
    public Float asFloat() {
        return ((Number) obj).floatValue();
    }

    /**
     * Get Object as Float List
     *
     * @return Float List
     */
    public List<Float> asFloatList() {
        return (List<Float>) obj;
    }

    /**
     * Get Object as Integer
     *
     * @return Integer
     */
    public Integer asInt() {
        return ((Number) obj).intValue();
    }

    /**
     * Get Object as Integer List
     *
     * @return Integer List
     */
    public List<Integer> asIntList() {
        return (List<Integer>) obj;
    }

    /**
     * Get Object as Long
     *
     * @return Long
     */
    public Long asLong() {
        return ((Number) obj).longValue();
    }

    /**
     * Get Object as Long List
     *
     * @return Long List
     */
    public List<Long> asLongList() {
        return (List<Long>) obj;
    }

    /**
     * Get a Short by Handle
     *
     * @return Short
     */
    public Short asShort() {
        return ((Number) obj).shortValue();
    }

    /**
     * Get a Short List by Handle
     *
     * @return Short List
     */
    public List<Short> asShortList() {
        return (List<Short>) obj;
    }

    /**
     * Get Object as String
     *
     * @return String
     */
    public String asString() {
        if (obj != null) return obj.toString();
        else return null;
    }

    /**
     * Get Object as String List
     *
     * @return String List
     */
    public List<String> asStringList() {
        if (obj != null) {
            List<String> values = new LinkedList<>();
            for (Object value : (List<?>) obj) {
                values.add((value == null)?null:value.toString());
            }
            return values;
        } else return null;
    }

    /**
     * Get Object as UUID
     *
     * @return UUID
     */
    public UUID asUUID() {
        return parseUUID(obj);
    }
    private static UUID parseUUID(Object obj) {
        if (obj instanceof Collection) {
            Iterator<Number> i = ((Collection<Number>) obj).iterator();
            return new UUID(i.next().longValue(), i.next().longValue());
        } else if (obj != null) {
            return UUID.fromString(obj.toString());
        } else {
            return null;
        }
    }

    /**
     * Get Object as UUID List
     *
     * @return UUID List
     */
    public List<UUID> asUUIDList() {
        if (obj != null) {
            List<UUID> values = new LinkedList<UUID>();
            for (Object value : (List<?>) obj) {
                values.add(parseUUID(value));
            }
            return values;
        } else return null;
    }

    /**
     * Get Object as Version
     *
     * @return Version
     */
    public Version asVersion() {
        if (obj != null) return Version.fromString(asString());
        else return null;
    }

    /**
     * Get Object as Version List
     *
     * @return Version List
     */
    public List<Version> asVersionList() {
        if (obj != null) {
            List<Version> values = new LinkedList<Version>();
            for (String value : asStringList()) {
                values.add((value == null)?null:Version.fromString(value));
            }
            return values;
        } else return null;
    }

    /**
     * Check if object is Null
     *
     * @return Null Status
     */
    public boolean isNull() {
        return obj == null;
    }

    /**
     * Check if object is a Boolean
     *
     * @return Boolean Status
     */
    public boolean isBoolean() {
        return (obj instanceof Boolean);
    }

    /**
     * Check if object is an Object Map
     *
     * @return ObjectMap Status
     */
    public boolean isMap() {
        return (obj instanceof ObjectMap);
    }

    /**
     * Check if object is a List
     *
     * @return List Status
     */
    public boolean isList() {
        return (obj instanceof List);
    }

    /**
     * Check if object is a Number
     *
     * @return Number Status
     */
    public boolean isNumber() {
        return (obj instanceof Number);
    }

    /**
     * Check if object is a String
     *
     * @return String Status
     */
    public boolean isString() {
        return (obj instanceof String);
    }

    /**
     * Check if object is a UUID
     *
     * @return UUID Status
     */
    public boolean isUUID() {
        if (obj instanceof Collection) {
            Iterator<Number> i = ((Collection<Number>) obj).iterator();
            if (i.hasNext()) {
                i.next();
                return i.hasNext();
            }
        } else if (obj instanceof String) {
            return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object object) {
        if (obj == null) {
            return object == null;
        } else {
            if (object instanceof ObjectMapValue) {
                return obj.equals(((ObjectMapValue) object).obj);
            } else {
                return obj.equals(object);
            }
        }
    }

    @Override
    public String toString() {
        if (obj != null) return obj.toString();
        else return "null";
    }
}
