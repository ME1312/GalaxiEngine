package net.ME1312.Galaxi.Library.Config;

import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * YAML Config Section Class
 */
@SuppressWarnings({"unchecked", "unused"})
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
        setAll((LinkedHashMap<String, Object>) (this.yaml = new Yaml(YAMLConfig.getDumperOptions())).loadAs(stream, LinkedHashMap.class));
    }

    /**
     * Creates a YAML Section from a Reader
     *
     * @param reader Reader
     * @throws YAMLException
     */
    public YAMLSection(Reader reader) throws YAMLException {
        if (Util.isNull(reader)) throw new NullPointerException();
        setAll((LinkedHashMap<String, Object>) (this.yaml = new Yaml(YAMLConfig.getDumperOptions())).loadAs(reader, LinkedHashMap.class));
    }

    /**
     * Creates a YAML Section from String
     *
     * @param str String
     * @throws YAMLException
     */
    public YAMLSection(String str) throws YAMLException {
        if (Util.isNull(str)) throw new NullPointerException();
        setAll((LinkedHashMap<String, Object>) (this.yaml = new Yaml(YAMLConfig.getDumperOptions())).loadAs(str, LinkedHashMap.class));
    }

    /**
     * Creates a YAML Section from Map Contents
     *
     * @param map Map
     */
    public YAMLSection(Map<String, ?> map) {
        if (Util.isNull(map)) throw new NullPointerException();
        this.yaml = new Yaml(YAMLConfig.getDumperOptions());

        setAll(map);
    }

    /**
     * Creates a YAML Section from Map Contents
     *
     * @param map Map
     */
    public YAMLSection(ObjectMap<String> map) {
        this(map.get());
    }

    /**
     * Creates a YAML Section from JSON Contents
     *
     * @param json JSON
     */
    public YAMLSection(JSONObject json) {
        if (Util.isNull(json)) throw new NullPointerException();
        setAll((LinkedHashMap<String, Object>) (this.yaml = new Yaml(YAMLConfig.getDumperOptions())).loadAs(json.toString(4), LinkedHashMap.class));
    }

    YAMLSection(Map<String, ?> map, YAMLSection up, String handle, Yaml yaml) {
        this.yaml = yaml;

        if (map != null) setAll(map);
    }

    /**
     * Clone this Map
     *
     * @return Map Clone
     */
    public YAMLSection clone() {
        return new YAMLSection(map);
    }

    @Override
    public String toString() {
        return yaml.dump(map);
    }

    /**
     * Convert to JSON
     *
     * @return JSON
     */
    public JSONObject toJSON() {
        return new JSONObject(map);
    }
}
