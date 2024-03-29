package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Galaxi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

class PluginClassLoader extends URLClassLoader {
    private static final HashMap<File, PluginClassLoader> loaders = new HashMap<File, PluginClassLoader>();
    private Class<?> defaultClass = null;
    private final ClassLoader parent;
    private final File file;

    static PluginClassLoader get(File file) {
        return get(null, file);
    }

    static PluginClassLoader get(ClassLoader parent, File file) {
        if (!loaders.containsKey(file)) loaders.put(file, new PluginClassLoader(parent, file));
        return loaders.get(file);
    }

    private PluginClassLoader(ClassLoader parent, File file) {
        super(toSuper(file), null);
        this.parent = parent;
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
            Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
        }
        return (result == null)?new URL[0]:new URL[]{result};
    }

    File getFile() {
        return file;
    }

    void setDefaultClass(Class<?> clazz) {
        defaultClass = clazz;
    }

    Class<?> getDefaultClass() throws ClassNotFoundException {
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

    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return this.loadClass(name, (byte) 1);
    }

    private Class<?> loadClass(String name, byte level) throws ClassNotFoundException {
        boolean check = level > 0;
        try {
            return super.loadClass(name);
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            if (parent != null) try {
                return parent.loadClass(name);
            } catch (NoClassDefFoundError | ClassNotFoundException e2) {}

            if (check) {
                Iterator i = new LinkedList<>(loaders.values()).iterator();

                for (;;) {
                    PluginClassLoader loader;
                    do {
                        if (!i.hasNext()) {
                            return getDefaultClass(name);
                        }
                        loader = (PluginClassLoader) i.next();
                    } while (loader == this);

                    try {
                        return loader.loadClass(name, (byte) 0);
                    } catch (NoClassDefFoundError | ClassNotFoundException e3) {}
                }
            } else {
                throw new ClassNotFoundException(name);
            }
        }
    }
}