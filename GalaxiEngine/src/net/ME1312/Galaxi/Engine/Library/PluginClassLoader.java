package net.ME1312.Galaxi.Engine.Library;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Plugin ClassLoader Class
 */
public class PluginClassLoader extends URLClassLoader {
    private static Set<PluginClassLoader> loaders = new CopyOnWriteArraySet<PluginClassLoader>();
    private Class<?> defaultClass = null;
    private File[] files;

    /**
     * Load Classes from URLs
     *
     * @param files Files
     */
    public PluginClassLoader(File[] files) {
        super(toSuper(files));
        this.files = files;
        loaders.add(this);
    }

    /**
     * Load Classes from URLs with a parent loader
     *
     * @param parent Parent loader
     * @param files Files
     */
    public PluginClassLoader(ClassLoader parent, File... files) {
        super(toSuper(files), parent);
        this.files = files;
        loaders.add(this);
    }

    /*
     * Convert File[] to URL[] for Super
     */
    private static URL[] toSuper(File[] files) {
        URL[] result = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            try {
                result[i] = files[i].toURI().toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Get the files used by this ClassLoader
     *
     * @return Loaded Files
     */
    public File[] getFiles() {
        return files.clone();
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
                Iterator i = loaders.iterator();

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