package net.ME1312.Galaxi.Library.Map;

import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;

import java.util.*;

/**
 * Object Map Class
 *
 * @param <K> Key Type
 */
@SuppressWarnings({"unchecked", "unused", "rawtypes"})
public class ObjectMap<K> {
    private static final Object x = null;
    LinkedHashMap<K, ObjectMapValue<K>> map;
    ObjectMap<K> up;
    K label;

    /**
     * Creates an empty Object Map
     */
    public ObjectMap() {
        this((Map<K, ?>) null);
    }

    /**
     * Creates an Object Map from Map Contents
     *
     * @param map Map
     */
    public ObjectMap(Map<? extends K, ?> map) {
        this.map = new LinkedHashMap<>();
        if (map != null) setAll(map);
    }

    /**
     * Creates an Object Map from Map Contents
     *
     * @param map Map
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public ObjectMap(ObjectMap<? extends K> map) {
        this(map.map);
    }


    /**
     * Get a copy of the original Object Map
     *
     * @return Object Map
     */
    public Map<K, ?> get() {
        return (Map<K, ?>) simplify(this);
    }

    /**
     * Clone this Map
     *
     * @return Map Clone
     */
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public ObjectMap<K> clone() {
        return constructMap(map);
    }

    /**
     * Go up a level in the config (or null if this is the top layer)
     *
     * @return Super Map
     */
    public ObjectMap<K> getParent() {
        return up;
    }

    /**
     * Change the Key type of this map
     *
     * @param <T> Key Type
     * @return Object Map
     */
    public <T> ObjectMap<T> key() {
        return (ObjectMap<T>) this;
    }

    /**
     * Get the Keys
     *
     * @return KeySet
     */
    public Set<K> getKeys() {
        return map.keySet();
    }

    /**
     * Get the Values
     *
     * @return Values
     */
    public Collection<ObjectMapValue<K>> getValues() {
        return map.values();
    }

    /**
     * Get the Entries
     *
     * @return Entries
     */
    public Collection<Map.Entry<K, ObjectMapValue<K>>> getEntries() {
        return map.entrySet();
    }

    /**
     * Check if a Handle exists
     *
     * @param handle Handle
     * @return if that handle exists
     */
    public boolean contains(K handle) {
        return map.keySet().contains(handle);
    }

    /**
     * Wrap a Map in an ObjectMap
     *
     * @param map Map
     * @return ObjectMap
     */
    protected ObjectMap<K> constructMap(Map<? extends K, ?> map) {
        return new ObjectMap<>(map);
    }

    /**
     * Wrap an Object in an ObjectMapValue
     *
     * @param value Object
     * @return ObjectMapValue
     */
    protected ObjectMapValue<K> constructValue(Object value) {
        return new ObjectMapValue<>(value);
    }

    /**
     * Convert from raw formatting
     *
     * @param value Value to convert
     * @return Converted Value
     */
    protected Object complicate(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Map) {
            return constructMap((Map) value);
        } else if (value instanceof ObjectMap) {
            if (((ObjectMap) value).up != this && (((ObjectMap) value).up != null || ((ObjectMap) value).label != null))
                return ((ObjectMap) value).clone(); // Clone sub-maps that belong to other maps
            return value;
        } else if (value instanceof Collection) {
            List<Object> list = new LinkedList<>();
            for (Object val : (Collection<Object>) value) list.add(complicate(val));
            return list;
        } else if (value.getClass().isArray()) {
            List<Object> list = new LinkedList<Object>();
            for (int i = 0; i < ((Object[]) value).length; i++) list.add(complicate(((Object[]) value)[i]));
            return list;
        } else if (value instanceof UUID) {
            return value.toString();
        } else if (value instanceof Version) {
            return ((Version) value).toFullString();
        } else {
            return value;
        }
    }
    ObjectMapValue<K> wrap(K key, Object value) {
        ObjectMapValue<K> wrapped = constructValue(complicate((value instanceof ObjectMapValue) ? ((ObjectMapValue) value).obj : value));
        if (wrapped.isMap()) {
            wrapped.asMap().up = this;
            wrapped.asMap().label = key;
        }
        wrapped.up = this;
        wrapped.label = key;
        return wrapped;
    }

    /**
     * Convert to raw formatting
     *
     * @param value Value to convert
     * @return Converted Value
     */
    protected Object simplify(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof ObjectMap) {
            LinkedHashMap<Object, Object> map = new LinkedHashMap<>();
            for (Map.Entry<Object, ObjectMapValue> e : (Collection<Map.Entry<Object, ObjectMapValue>>) ((ObjectMap) value).getEntries()) {
                map.put(e.getKey(), (e.getValue() == null) ? null : simplify(e.getValue().obj));
            }
            return map;
        } else if (value instanceof ObjectMapValue) {
            return simplify(((ObjectMapValue) value).obj);
        } else if (value instanceof Collection) {
            List<Object> list = new LinkedList<>();
            for (Object val : (Collection<Object>) value) list.add(simplify(val));
            return list;
        } else {
            return value;
        }
    }

    /**
     * Set Object into this Map
     *
     * @param handle Handle
     * @param value Value
     */
    public synchronized void set(K handle, Object value) {
        if (Util.isNull(handle)) throw new NullPointerException();
        map.put(handle, wrap(handle, value));
    }

    /**
     * Set Object into this Map without overwriting existing value
     *
     * @param handle Handle
     * @param value Value
     */
    public void safeSet(K handle, Object value) {
        if (Util.isNull(handle)) throw new NullPointerException();
        if (!contains(handle)) set(handle, value);
    }

    /**
     * Set All Objects into this Map
     *
     * @param values Map to set
     */
    public void setAll(Map<? extends K, ?> values) {
        if (Util.isNull(values)) throw new NullPointerException();
        for (K value : values.keySet()) {
            set(value, values.get(value));
        }
    }

    /**
     * Set All Objects into this Map without overwriting existing values
     *
     * @param values Map to set
     */
    public void safeSetAll(Map<? extends K, ?> values) {
        if (Util.isNull(values)) throw new NullPointerException();
        for (K value : values.keySet()) {
            safeSet(value, values.get(value));
        }
    }

    /**
     * Copy All Values to this Map
     *
     * @param values Object Map to merge
     */
    public void setAll(ObjectMap<? extends K> values) {
        if (Util.isNull(values)) throw new NullPointerException();
        setAll(values.map);
    }

    /**
     * Copy All to this Map without overwriting existing values
     *
     * @param values Object Map to merge
     */
    public void safeSetAll(ObjectMap<? extends K> values) {
        if (Util.isNull(values)) throw new NullPointerException();
        safeSetAll(values.map);
    }

    /**
     * Remove an Object by Handle
     *
     * @param handle Handle
     */
    public synchronized void remove(K handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        map.remove(handle);
    }

    /**
     * Remove all Objects from this Map
     */
    public void clear() {
        map.clear();
    }

    /**
     * Get an Object by Handle
     *
     * @param handle Handle
     * @return Object
     */
    public ObjectMapValue<K> get(K handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return map.get(handle);
    }

    /**
     * Get an Object by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object
     */
    public ObjectMapValue<K> get(K handle, Object def) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return (map.get(handle) != null)? map.get(handle):wrap(handle, def);
    }

    /**
     * Get an Object by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object
     */
    public ObjectMapValue<K> get(K handle, ObjectMapValue<K> def) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return (map.get(handle) != null)? map.get(handle):def;
    }

    /**
     * Get a List by Handle
     *
     * @param handle Handle
     * @return Object
     */
    public List<ObjectMapValue<K>> getList(K handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return map.get(handle).asList();
    }

    /**
     * Get a List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object List
     */
    public List<ObjectMapValue<K>> getList(K handle, Collection<?> def) {
        if (Util.isNull(handle)) throw new NullPointerException();
        if (map.get(handle) != null) {
            return getList(handle);
        } else if (def != null) {
            List<ObjectMapValue<K>> values = new ArrayList<ObjectMapValue<K>>();
            for (Object value : def) {
                values.add(wrap(null, value));
            }
            return values;
        } else return null;
    }

    /**
     * Get a List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object List
     */
    public List<ObjectMapValue<K>> getList(K handle, List<? extends ObjectMapValue<K>> def) {
        if (Util.isNull(handle)) throw new NullPointerException();
        if (map.get(handle) != null) {
            return getList(handle);
        } else if (def != null) {
            return (List<ObjectMapValue<K>>) def;
        } else return null;
    }

    /**
     * Get a Object by Handle
     *
     * @param handle Handle
     * @return Object
     */
    public Object getObject(K handle) {
        return get(handle, x).asObject();
    }

    /**
     * Get a Object by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object
     */
    public Object getObject(K handle, Object def) {
        return get(handle, def).asObject();
    }

    /**
     * Get a Object List by Handle
     *
     * @param handle Handle
     * @return Object List
     */
    public List<?> getObjectList(K handle) {
        return get(handle, x).asObjectList();
    }

    /**
     * Get a Object List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object List
     */
    public List<?> getObjectList(K handle, List<?> def) {
        return get(handle, def).asObjectList();
    }

    /**
     * Get a Boolean by Handle
     *
     * @param handle Handle
     * @return Boolean
     */
    public Boolean getBoolean(K handle) {
        return get(handle, x).asBoolean();
    }

    /**
     * Get a Boolean by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Boolean
     */
    public Boolean getBoolean(K handle, Boolean def) {
        return get(handle, def).asBoolean();
    }

    /**
     * Get a Boolean List by Handle
     *
     * @param handle Handle
     * @return Boolean List
     */
    public List<Boolean> getBooleanList(K handle) {
        return get(handle, x).asBooleanList();
    }

    /**
     * Get a Boolean List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Boolean List
     */
    public List<Boolean> getBooleanList(K handle, List<Boolean> def) {
        return get(handle, def).asBooleanList();
    }

    /**
     * Get an Object Map by Handle
     *
     * @param handle Handle
     * @return Object Map
     */
    public ObjectMap<K> getMap(K handle) {
        return get(handle, x).asMap();
    }

    /**
     * Get an Object Map by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map
     */
    public ObjectMap<K> getMap(K handle, Map<? extends K, ?> def) {
        return get(handle, def).asMap();
    }

    /**
     * Get an Object Map by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map
     */
    public ObjectMap<K> getMap(K handle, ObjectMap<? extends K> def) {
        if (Util.isNull(handle)) throw new NullPointerException();
        if (map.get(handle) != null) {
            return getMap(handle);
        } else if (def != null) {
            return (ObjectMap<K>) def;
        } else return null;
    }

    /**
     * Get an Object Map List by Handle
     *
     * @param handle Handle
     * @return Object Map List
     */
    public List<ObjectMap<K>> getMapList(K handle) {
        return get(handle, x).asMapList();
    }

    /**
     * Get an Object Map List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map List
     */
    public List<ObjectMap<K>> getMapList(K handle, Collection<? extends Map<? extends K, ?>> def) {
        return get(handle, def).asMapList();
    }

    /**
     * Get an Object Map List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map List
     */
    public List<ObjectMap<K>> getMapList(K handle, List<? extends ObjectMap<? extends K>> def) {
        if (Util.isNull(handle)) throw new NullPointerException();
        if (map.get(handle) != null) {
            return get(handle).asMapList();
        } else if (def != null) {
            return (List<ObjectMap<K>>) def;
        } else return null;
    }

    /**
     * Get a Double by Handle
     *
     * @param handle Handle
     * @return Double
     */
    public Double getDouble(K handle) {
        return get(handle, x).asDouble();
    }

    /**
     * Get a Double by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Double
     */
    public Double getDouble(K handle, Double def) {
        return get(handle, def).asDouble();
    }

    /**
     * Get a Double List by Handle
     *
     * @param handle Handle
     * @return Double List
     */
    public List<Double> getDoubleList(K handle) {
        return get(handle, x).asDoubleList();
    }

    /**
     * Get a Double List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Double List
     */
    public List<Double> getDoubleList(K handle, List<Double> def) {
        return get(handle, def).asDoubleList();
    }

    /**
     * Get a Float by Handle
     *
     * @param handle Handle
     * @return Float
     */
    public Float getFloat(K handle) {
        return get(handle, x).asFloat();
    }

    /**
     * Get a Float by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Float
     */
    public Float getFloat(K handle, Float def) {
        return get(handle, def).asFloat();
    }

    /**
     * Get a Float List by Handle
     *
     * @param handle Handle
     * @return Float List
     */
    public List<Float> getFloatList(K handle) {
        return get(handle, x).asFloatList();
    }

    /**
     * Get a Float List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Float List
     */
    public List<Float> getFloatList(K handle, List<Float> def) {
        return get(handle, def).asFloatList();
    }

    /**
     * Get an Integer by Handle
     *
     * @param handle Handle
     * @return Integer
     */
    public Integer getInt(K handle) {
        return get(handle, x).asInt();
    }

    /**
     * Get an Integer by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Integer
     */
    public Integer getInt(K handle, Integer def) {
        return get(handle, def).asInt();
    }

    /**
     * Get an Integer List by Handle
     *
     * @param handle Handle
     * @return Integer List
     */
    public List<Integer> getIntList(K handle) {
        return get(handle, x).asIntList();
    }

    /**
     * Get an Integer List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Integer List
     */
    public List<Integer> getIntList(K handle, List<Integer> def) {
        return get(handle, def).asIntList();
    }

    /**
     * Get a Long by Handle
     *
     * @param handle Handle
     * @return Long
     */
    public Long getLong(K handle) {
        return get(handle, x).asLong();
    }

    /**
     * Get a Long by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Long
     */
    public Long getLong(K handle, Long def) {
        return get(handle, def).asLong();
    }

    /**
     * Get a Long List by Handle
     *
     * @param handle Handle
     * @return Long List
     */
    public List<Long> getLongList(K handle) {
        return get(handle, x).asLongList();
    }

    /**
     * Get a Long List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Long List
     */
    public List<Long> getLongList(K handle, List<Long> def) {
        return get(handle, def).asLongList();
    }

    /**
     * Get a Short by Handle
     *
     * @param handle Handle
     * @return Short
     */
    public Short getShort(K handle) {
        return get(handle, x).asShort();
    }

    /**
     * Get a Short by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Short
     */
    public Short getShort(K handle, Short def) {
        return get(handle, def).asShort();
    }

    /**
     * Get a Short List by Handle
     *
     * @param handle Handle
     * @return Short List
     */
    public List<Short> getShortList(K handle) {
        return get(handle, x).asShortList();
    }

    /**
     * Get a Short List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Short List
     */
    public List<Short> getShortList(K handle, List<Short> def) {
        return get(handle, def).asShortList();
    }

    /**
     * Get an Unparsed String by Handle
     *
     * @param handle Handle
     * @return Unparsed String
     */
    public String getRawString(K handle) {
        return get(handle, x).asRawString();
    }

    /**
     * Get an Unparsed String by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Unparsed String
     */
    public String getRawString(K handle, String def) {
        return get(handle, def).asRawString();
    }

    /**
     * Get an Unparsed String List by Handle
     *
     * @param handle Handle
     * @return Unparsed String List
     */
    public List<String> getRawStringList(K handle) {
        return get(handle, x).asRawStringList();
    }

    /**
     * Get an Unparsed String List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Unparsed String List
     */
    public List<String> getRawStringList(K handle, List<String> def) {
        return get(handle, def).asRawStringList();
    }

    /**
     * Get a String by Handle
     *
     * @param handle Handle
     * @return String
     */
    public String getString(K handle) {
        return get(handle, x).asString();
    }

    /**
     * Get a String by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return String
     */
    public String getString(K handle, String def) {
        return get(handle, def).asString();
    }

    /**
     * Get a String List by Handle
     *
     * @param handle Handle
     * @return String List
     */
    public List<String> getStringList(K handle) {
        return get(handle, x).asStringList();
    }

    /**
     * Get a String List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return String List
     */
    public List<String> getStringList(K handle, List<String> def) {
        return get(handle, def).asStringList();
    }

    /**
     * Get a UUID by Handle
     *
     * @param handle Handle
     * @return UUID
     */
    public UUID getUUID(K handle) {
        return get(handle, x).asUUID();
    }

    /**
     * Get a UUID by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return UUID
     */
    public UUID getUUID(K handle, UUID def) {
        return get(handle, def).asUUID();
    }

    /**
     * Get a UUID List by Handle
     *
     * @param handle Handle
     * @return UUID List
     */
    public List<UUID> getUUIDList(K handle) {
        return get(handle, x).asUUIDList();
    }

    /**
     * Get a UUID List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return UUID List
     */
    public List<UUID> getUUIDList(K handle, List<UUID> def) {
        return get(handle, def).asUUIDList();
    }

    /**
     * Get a Version by Handle
     *
     * @param handle Handle
     * @return Version
     */
    public Version getVersion(K handle) {
        return get(handle, x).asVersion();
    }

    /**
     * Get a Version by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Version
     */
    public Version getVersion(K handle, Version def) {
        return get(handle, def).asVersion();
    }

    /**
     * Get a Version List by Handle
     *
     * @param handle Handle
     * @return Version List
     */
    public List<Version> getVersionList(K handle) {
        return get(handle, x).asVersionList();
    }

    /**
     * Get a Version List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Version List
     */
    public List<Version> getVersionList(K handle, List<Version> def) {
        return get(handle, def).asVersionList();
    }

    /**
     * Check if object is Null by Handle
     *
     * @param handle Handle
     * @return Object Null Status
     */
    public boolean isNull(K handle) {
        return get(handle, x).isNull();
    }

    /**
     * Check if object is a Boolean by Handle
     *
     * @param handle Handle
     * @return Object Boolean Status
     */
    public boolean isBoolean(K handle) {
        return get(handle, x).isBoolean();
    }

    /**
     * Check if object is an Object Map by Handle
     *
     * @param handle Handle
     * @return Object Map Status
     */
    public boolean isMap(K handle) {
        return get(handle, x).isMap();
    }

    /**
     * Check if object is a List by Handle
     *
     * @param handle Handle
     * @return Object List Status
     */
    public boolean isList(K handle) {
        return get(handle, x).isList();
    }

    /**
     * Check if object is a Number by Handle
     *
     * @param handle Handle
     * @return Number Status
     */
    public boolean isNumber(K handle) {
        return get(handle, x).isNumber();
    }

    /**
     * Check if object is a String by Handle
     *
     * @param handle Handle
     * @return Object String Status
     */
    public boolean isString(K handle) {
        return get(handle, x).isString();
    }

    /**
     * Check if object is a UUID by Handle
     *
     * @param handle Handle
     * @return Object UUID Status
     */
    public boolean isUUID(K handle) {
        return get(handle, x).isUUID();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ObjectMap) {
            return map.equals(((ObjectMap) object).map);
        } else {
            return super.equals(object);
        }
    }

    @Override
    public String toString() {
        if (map != null) return map.toString();
        else return "null";
    }
}
