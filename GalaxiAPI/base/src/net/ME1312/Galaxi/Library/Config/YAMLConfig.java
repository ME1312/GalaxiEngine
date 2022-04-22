package net.ME1312.Galaxi.Library.Config;

import net.ME1312.Galaxi.Library.Try;
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
        Util.nullpo(file);
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
        Util.nullpo(value);
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
        Try.all.run(() -> options.setAllowUnicode(false));
        Try.all.run(() -> options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK));
        Try.all.run(() -> options.setSplitLines(false));
        Try.all.run(() -> options.setIndent(2));

        return options;
    }
}