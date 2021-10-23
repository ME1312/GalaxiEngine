package net.ME1312.Galaxi.Library.Config;

import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;

import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * YAML Config Section Class
 */
@SuppressWarnings({"unchecked", "unused", "rawtypes"})
public class YAMLSection extends ObjectMap<String> {
    private Yaml yaml;

    /**
     * Creates an empty YAML Section
     */
    public YAMLSection() {
        this.yaml = new Yaml(YAMLConfig.getDumperOptions());
    }

    /**
     * Creates a YAML Section from an Input Stream
     *
     * @param stream Input Stream
     * @throws YAMLException
     */
    public YAMLSection(InputStream stream) throws YAMLException {
        Util.nullpo(stream);
        setAll((LinkedHashMap<String, Object>) (this.yaml = new Yaml(YAMLConfig.getDumperOptions())).loadAs(stream, LinkedHashMap.class));
    }

    /**
     * Creates a YAML Section from a Reader
     *
     * @param reader Reader
     * @throws YAMLException
     */
    public YAMLSection(Reader reader) throws YAMLException {
        Util.nullpo(reader);
        setAll((LinkedHashMap<String, Object>) (this.yaml = new Yaml(YAMLConfig.getDumperOptions())).loadAs(reader, LinkedHashMap.class));
    }

    /**
     * Creates a YAML Section from a String
     *
     * @param str String
     * @throws YAMLException
     */
    public YAMLSection(String str) throws YAMLException {
        Util.nullpo(str);
        setAll((LinkedHashMap<String, Object>) (this.yaml = new Yaml(YAMLConfig.getDumperOptions())).loadAs(str, LinkedHashMap.class));
    }

    /**
     * Creates a YAML Section from Map Contents
     *
     * @param map Map
     */
    public YAMLSection(Map<? extends String, ?> map) {
        this(map, new Yaml(YAMLConfig.getDumperOptions()));
    }

    /**
     * Creates a YAML Section from Map Contents
     *
     * @param map Map
     */
    public YAMLSection(ObjectMap<String> map) {
        super(map);
        this.yaml = (map instanceof YAMLSection) ? ((YAMLSection) map).yaml : new Yaml(YAMLConfig.getDumperOptions());
    }

    /**
     * Creates a YAML Section from JSON Contents
     *
     * @param json JSON
     */
    public YAMLSection(JSONObject json) {
        this(json.toString(4));
    }

    YAMLSection(Map<? extends String, ?> map, Yaml yaml) {
        super(map);
        this.yaml = yaml;
    }

    @Override
    public YAMLSection clone() {
        return (YAMLSection) super.clone();
    }

    @Override
    protected ObjectMap<String> constructMap(Map<? extends String, ?> map) {
        return new YAMLSection(map, yaml);
    }

    /**
     * Get a YAML Section by Handle
     *
     * @param handle Handle
     * @return Object Map
     */
    public YAMLSection getSection(String handle) {
        return (YAMLSection) super.getMap(handle);
    }

    /**
     * Get a YAML Section by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map
     */
    public YAMLSection getSection(String handle, Map<? extends String, ?> def) {
        return (YAMLSection) super.getMap(handle, def);
    }

    /**
     * Get a YAML Section by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map
     */
    public YAMLSection getSection(String handle, ObjectMap<? extends String> def) {
        return (YAMLSection) super.getMap(handle, def);
    }

    /**
     * Get a YAML Section List by Handle
     *
     * @param handle Handle
     * @return Object Map List
     */
    public List<YAMLSection> getSectionList(String handle) {
        return (List<YAMLSection>) (List) super.getMapList(handle);
    }

    /**
     * Get a YAML Section List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map List
     */
    public List<YAMLSection> getSectionList(String handle, Collection<? extends Map<? extends String, ?>> def) {
        return (List<YAMLSection>) (List) super.getMapList(handle, def);
    }

    /**
     * Get a YAML Section List by Handle
     *
     * @param handle Handle
     * @param def Default
     * @return Object Map List
     */
    public List<YAMLSection> getSectionList(String handle, List<? extends ObjectMap<? extends String>> def) {
        return (List<YAMLSection>) (List) super.getMapList(handle, def);
    }

    @Override
    public synchronized String toString() {
        return yaml.dump(get());
    }

    /**
     * Convert to JSON
     *
     * @return JSON
     */
    public JSONObject toJSON() {
        return new JSONObject(get());
    }
}
