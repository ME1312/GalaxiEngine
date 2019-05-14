package net.ME1312.Galaxi.Engine.Library;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Plugin ClassLoader Class
 */
public class PluginClassLoader extends URLClassLoader {
    private static HashMap<File, PluginClassLoader> loaders = new HashMap<File, PluginClassLoader>();
    private Class<?> defaultClass = null;
    private File file;

    /**
     * Load Classes from URLs
     *
     * @param file File
     */
    public static PluginClassLoader get(File file) {
        return get(null, file);
    }

    /**
     * Load Classes from URLs with a parent loader
     *
     * @param parent Parent loader
     * @param file File
     */
    public static PluginClassLoader get(ClassLoader parent, File file) {
        if (!loaders.keySet().contains(file)) loaders.put(file, new PluginClassLoader(parent, file));
        return loaders.get(file);
    }

    private PluginClassLoader(ClassLoader parent, File file) {
        super(toSuper(file), parent);
        this.file = file;

        loaders.put(file, this);
    }

    /*
     * Convert File[] to URL[] for Super
     */
    private static URL[] toSuper(File file) {
        URL result = null;
        try {
            result = file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return (result == null)?new URL[0]:new URL[]{result};
    }

    /**
     * Get the file used by this ClassLoader
     *
     * @return Loaded File
     */
    public File getFile() {
        return file;
    }

    /**
     * Set the Default Class to load when no class is found
     *
     * @param clazz Class
     */
    public void setDefaultClass(Class<?> clazz) {
        defaultClass = clazz;
    }


    /**
     * Get the Default Class to load when no class is found
     *
     * @throws ClassNotFoundException when no Default Class is set
     * @return Default Class
     */
    public Class<?> getDefaultClass() throws ClassNotFoundException {
        if (defaultClass == null) {
            throw new ClassNotFoundException();
        } else {
            return defaultClass;
        }
    }

    private Class<?> getDefaultClass(String name) throws ClassNotFoundException {
        try {
            return getDefaultClass();
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException(name);
        }
    }

    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return this.loadClass(name, resolve, true);
    }

    private Class<?> loadClass(String name, boolean resolve, boolean check) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            if (check) {
                Iterator i = new LinkedList<>(loaders.values()).iterator();

                while (true) {
                    PluginClassLoader loader;
                    do {
                        if (!i.hasNext()) {
                            return getDefaultClass(name);
                        }
                        loader = (PluginClassLoader) i.next();
                    } while (loader == this);

                    try {
                        return loader.loadClass(name, resolve, false);
                    } catch (NoClassDefFoundError | ClassNotFoundException ex) {}
                }
            } else {
                throw new ClassNotFoundException(name);
            }
        }
    }
}