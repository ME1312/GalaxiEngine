package net.ME1312.Galaxi.Library.Config;

import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.*;
import java.util.LinkedHashMap;

/**
 * YAML Config Class
 */
@SuppressWarnings("unused")
public class YAMLConfig {
    private File file;
    private Yaml yaml;
    private YAMLSection config;

    /**
     * Creates/Loads a YAML Formatted Config
     *
     * @param file
     * @throws IOException
     * @throws YAMLException
     */
    @SuppressWarnings("unchecked")
    public YAMLConfig(File file) throws IOException, YAMLException {
        if (Util.isNull(file)) throw new NullPointerException();
        this.file = file;
        this.yaml = new Yaml(getDumperOptions());
        if (file.exists()) {
            InputStream stream = new FileInputStream(file);
            this.config = new YAMLSection((LinkedHashMap<String, ?>) yaml.loadAs(stream, LinkedHashMap.class), yaml);
            stream.close();
        } else {
            this.config = new YAMLSection(null, yaml);
        }
    }

    /**
     * Get Config Contents
     *
     * @return Config Contents
     */
    public YAMLSection get() {
        return config;
    }

    /**
     * Set Config Contents
     *
     * @param value Value
     */
    public void set(YAMLSection value) {
        if (Util.isNull(value)) throw new NullPointerException();
        config = value;
    }

    /**
     * Reload Config Contents
     *
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public synchronized void reload() throws IOException {
        if (file.exists()) {
            InputStream stream = new FileInputStream(file);
            this.config = new YAMLSection((LinkedHashMap<String, ?>) yaml.loadAs(stream, LinkedHashMap.class), yaml);
            stream.close();
        } else {
            this.config = new YAMLSection(null, yaml);
        }
    }

    /**
     * Save Config Contents
     *
     * @throws IOException
     */
    public void save() throws IOException {
        final YAMLSection config = this.config;
        synchronized (config) {
            if (!file.exists()) file.createNewFile();
            FileWriter writer = new FileWriter(file);
            yaml.dump(config.get(), writer);
            writer.close();
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof YAMLConfig) {
            return get().equals(((YAMLConfig) object).get());
        } else {
            return super.equals(object);
        }
    }

    @Override
    public String toString() {
        return config.toString();
    }

    static DumperOptions getDumperOptions() {
        DumperOptions options = new DumperOptions();
        Util.isException(() -> options.setAllowUnicode(false));
        Util.isException(() -> options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK));
        Util.isException(() -> options.setSplitLines(false));
        Util.isException(() -> options.setIndent(2));

        return options;
    }
}