package net.ME1312.Galaxi.Engine.RT;

import net.ME1312.Galaxi.Command.Command;
import net.ME1312.Galaxi.Library.Container.ContainedPair;
import net.ME1312.Galaxi.Library.Exception.IllegalPluginException;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.Galaxi.Plugin.Dependency;
import net.ME1312.Galaxi.Plugin.Plugin;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

class CodeManager extends net.ME1312.Galaxi.Engine.CodeManager {
    final HashMap<String, ClassLoader> knownClasses = new HashMap<String, ClassLoader>();
    final HashMap<String, PluginInfo> plugins = new LinkedHashMap<String, PluginInfo>();
    final TreeMap<String, Command> commands = super.commands;
    private final Engine engine;

    CodeManager(Engine engine) {
        this.engine = engine;
    }

    /**
     * Catalog classes for a library
     *
     * @param clazz A class from the library to search
     */
    @Override
    public String[] catalogLibrary(Class<?> clazz) throws IOException {
        LinkedList<String> classes = new LinkedList<String>();
        try {
            URL source = clazz.getProtectionDomain().getCodeSource().getLocation();
            if (source != null && source.getProtocol().equals("file")) {
                File file = new File(source.toURI());

                if (file.isDirectory()) {
                    for (String entry : Util.<List<String>>getDespiteException(() -> Util.reflect(Util.class.getDeclaredMethod("zipsearch", File.class, File.class), null, file, file), null)) {
                        if (!(new File(file.getAbsolutePath() + File.separator + entry).isDirectory()) && entry.endsWith(".class")) {
                            String e = entry.substring(0, entry.length() - 6).replace(File.separatorChar, '.');
                            if (!knownClasses.keySet().contains(e)) knownClasses.put(e, clazz.getClassLoader());
                            if (!classes.contains(e)) classes.add(e);
                        }
                    }
                }
                if (file.isFile()) {
                    JarFile jarFile = new JarFile(file);
                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            String e = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                            if (!knownClasses.keySet().contains(e)) knownClasses.put(e, clazz.getClassLoader());
                            if (!classes.contains(e)) classes.add(e);
                        }
                    }
                }
            }
        } catch (URISyntaxException e) {}
        return classes.toArray(new String[0]);
    }

    @Override
    public int loadPlugins(File... directories) {
        LinkedList<File> pljars = new LinkedList<File>();
        for (File directory : directories) {
            directory.mkdirs();
            if (directory.exists() && directory.isDirectory()) for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".jar")) pljars.add(file);
            }
        }
        if (pljars.size() > 0) {

            /*
             * Load Jars & Find Main Classes
             * (Unordered)
             */
            LinkedHashMap<PluginClassLoader, ContainedPair<LinkedList<String>, LinkedHashMap<String, String>>> classes = new LinkedHashMap<PluginClassLoader, ContainedPair<LinkedList<String>, LinkedHashMap<String, String>>>();
            for (File file : pljars) {
                try {
                    JarFile jar = new JarFile(file);
                    Enumeration<JarEntry> entries = jar.entries();
                    PluginClassLoader loader = PluginClassLoader.get(this.getClass().getClassLoader(), file);

                    loader.setDefaultClass(ClassNotFoundException.class);
                    boolean isplugin = false;
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            String cname = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                            knownClasses.put(cname, loader);
                            try {
                                Class<?> clazz = loader.loadClass(cname);
                                if (clazz.isAnnotationPresent(Plugin.class)) {
                                    ContainedPair<LinkedList<String>, LinkedHashMap<String, String>> jarmap = (classes.keySet().contains(loader))?classes.get(loader):new ContainedPair<LinkedList<String>, LinkedHashMap<String, String>>(new LinkedList<String>(), new LinkedHashMap<>());
                                    for (Dependency dependency : clazz.getAnnotation(Plugin.class).dependencies()) jarmap.key().add(dependency.name());
                                    jarmap.value().put(clazz.getAnnotation(Plugin.class).name(), cname);
                                    classes.put(loader, jarmap);
                                    isplugin = true;
                                }
                            } catch (Throwable e) {
                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Couldn't load class: " + cname + " (" + file.getName() + ')'));
                            }
                        }
                    }
                    loader.setDefaultClass(null);

                    if (!isplugin) {
                        engine.getAppInfo().getLogger().info.println("Loaded Library: " + file.getName());
                    }
                    jar.close();
                } catch (Throwable e) {
                    engine.getAppInfo().getLogger().error.println(new InvocationTargetException(e, "Problem searching possible plugin jar: " + file.getName()));
                }
            }

            /*
             * Load Main Classes & Plugin Descriptions
             * (Ordered by Known Dependencies)
             */
            int progress = 1;
            HashMap<String, PluginInfo> plugins = new LinkedHashMap<String, PluginInfo>();
            while (classes.size() > 0) {
                LinkedHashMap<PluginClassLoader, LinkedList<String>> loaded = new LinkedHashMap<PluginClassLoader, LinkedList<String>>();
                for (PluginClassLoader loader : classes.keySet()) {
                    LinkedList<String> loadedlist = new LinkedList<String>();
                    for (String name : classes.get(loader).value().keySet()) {
                        boolean load = true;
                        for (String depend : classes.get(loader).key()) {
                            if (!plugins.keySet().contains(depend.toLowerCase())) {
                                load = progress <= 0;
                            }
                        }

                        if (load) {
                            String main = classes.get(loader).value().get(name);
                            try {
                                Class<?> clazz = loader.loadClass(main);
                                if (!clazz.isAnnotationPresent(Plugin.class))
                                    throw new ClassCastException("Cannot find plugin descriptor");

                                Object obj = clazz.getConstructor().newInstance();
                                try {
                                    PluginInfo plugin = PluginInfo.load(obj);
                                    if (plugins.keySet().contains(plugin.getName().toLowerCase())) {
                                        if (engine.getEngineInfo().getName().equalsIgnoreCase(plugin.getName())) {
                                            throw new IllegalStateException("Plugin name cannot be the same as the Engine's name");
                                        } else if (engine.getAppInfo().getName().equalsIgnoreCase(plugin.getName())) {
                                            throw new IllegalStateException("Plugin name cannot be the same as the App's name");
                                        } else {
                                            IllegalStateException e = new IllegalStateException("Duplicate Plugin: " + plugin.getName());
                                            if (plugins.get(plugin.getName().toLowerCase()).getVersion().compareTo(plugin.getVersion()) < 0) {
                                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Couldn't load plugin descriptor for main class: " + main));
                                            } else {
                                                throw e;
                                            }
                                        }
                                    }
                                    plugin.addExtra("galaxi.plugin.loadafter", new ArrayList<String>());
                                    if (loader.getFile() != null) {
                                        Util.reflect(PluginInfo.class.getDeclaredField("dir"), plugin, new File(loader.getFile().getParentFile(), plugin.getName()));
                                    }
                                    plugins.put(plugin.getName().toLowerCase(), plugin);
                                } catch (IllegalPluginException e) {
                                    engine.getAppInfo().getLogger().error.println(e);
                                } catch (Throwable e) {
                                    engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Couldn't load plugin descriptor for main class: " + main));
                                }
                            } catch (InvocationTargetException e) {
                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e.getTargetException(), "Unhandled exception occurred while loading main class: " + main));
                            } catch (ClassCastException e) {
                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Main class isn't annotated as a Plugin: " + main));
                            } catch (Throwable e) {
                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Couldn't load main class: " + main));
                            }
                            loadedlist.add(name);
                        }
                    }
                    if (loadedlist.size() > 0) loaded.put(loader, loadedlist);
                }
                progress = 0;
                for (PluginClassLoader loader : loaded.keySet()) {
                    ContainedPair<LinkedList<String>, LinkedHashMap<String, String>> jarmap = classes.get(loader);
                    progress++;
                    for (String main : loaded.get(loader)) jarmap.value().remove(main);
                    if (jarmap.value().size() > 0) {
                        classes.put(loader, jarmap);
                    } else {
                        classes.remove(loader);
                    }
                }
            }

            /*
             * Load Extra Plugin Settings
             */
            for (PluginInfo plugin : plugins.values()) {
                for (String loadbefore : plugin.getLoadBefore()) {
                    if (plugins.keySet().contains(loadbefore.toLowerCase())) {
                        List<String> loadafter = plugins.get(loadbefore.toLowerCase()).getExtra("galaxi.plugin.loadafter").asRawStringList();
                        loadafter.add(plugin.getName().toLowerCase());
                        plugins.get(loadbefore.toLowerCase()).addExtra("galaxi.plugin.loadafter", loadafter);
                    }
                }
            }

            /*
             * Register Plugins
             * (Ordered by LoadBefore & Dependencies)
             */
            int i = 0;
            int unstick = 0;
            while (plugins.size() > 0) {
                List<String> loaded = new ArrayList<String>();
                for (PluginInfo plugin : plugins.values()) {
                    try {
                        boolean load = true;
                        for (PluginInfo.Dependency depend : plugin.getDependencies()) {
                            IllegalStateException e = null;
                            if (plugins.keySet().contains(depend.getName().toLowerCase())) {
                                int dsv = (depend.isRequired())?2:3;
                                if (unstick < dsv) {
                                    load = false;
                                    break;
                                } else if (unstick == dsv) {
                                    e = new IllegalStateException("Infinite" + ((depend.isRequired())?"":" soft") + " dependency loop: " + plugin.getName() + " -> " + depend.getName());
                                }
                            } else if (engine.getEngineInfo().getName().equalsIgnoreCase(depend.getName()) || engine.getAppInfo().getName().equalsIgnoreCase(depend.getName()) || this.plugins.keySet().contains(depend.getName().toLowerCase())) {
                                Version version = (engine.getEngineInfo().getName().equalsIgnoreCase(depend.getName()))?engine.getEngineInfo().getVersion():((engine.getAppInfo().getName().equalsIgnoreCase(depend.getName()))?engine.getAppInfo().getVersion():this.plugins.get(depend.getName().toLowerCase()).getVersion());
                                if (depend.getMinVersion() == null || version.compareTo(depend.getMinVersion()) >= 0) {
                                    if (!(depend.getMaxVersion() == null || version.compareTo(depend.getMaxVersion()) < 0)) {
                                        e = new IllegalStateException("Dependency version is too new: " + depend.getName() + " v" + plugin.getVersion().toString() + " (should be below " + depend.getMaxVersion() + ")");
                                    }
                                } else {
                                    e = new IllegalStateException("Dependency version is too old: " + depend.getName() + " v" + plugin.getVersion().toString() + " (should be at or above " + depend.getMinVersion() + ")");
                                }
                            } else if (depend.isRequired()) {
                                String version = null;
                                if (depend.getMinVersion() != null && depend.getMaxVersion() != null) {
                                    version = depend.getMinVersion() + " - " + depend.getMaxVersion();
                                } else if (depend.getMaxVersion() != null) {
                                    version = "<" + depend.getMaxVersion();
                                } else if (depend.getMinVersion() != null) {
                                    version = depend.getMinVersion() + "+";
                                }

                                e = new IllegalStateException("Unknown dependency: " + depend.getName() + ((version == null)?"":" "+version));
                            }

                            if (e != null) {
                                if (depend.isRequired()) {
                                    throw new IllegalPluginException(e, "Cannot meet requirements for plugin: " + plugin.getName() + " v" + plugin.getVersion().toString());
                                } else {
                                    engine.getAppInfo().getLogger().warn.println(e);
                                }
                            }
                        }
                        for (String loadafter : plugin.getExtra("galaxi.plugin.loadafter").asRawStringList()) {
                            if (plugins.keySet().contains(loadafter.toLowerCase())) {
                                if (unstick < 1) {
                                    load = false;
                                    break;
                                } else if (unstick == 1) {
                                    engine.getAppInfo().getLogger().warn.println(new IllegalStateException("Infinite load before loop: " + loadafter + " -> " + plugin.getName()));
                                }
                            }
                        }
                        if (load) try {
                            plugin.removeExtra("galaxi.plugin.loadafter");
                            plugin.setEnabled(true);
                            registerListeners(plugin, plugin.get());
                            this.plugins.put(plugin.getName().toLowerCase(), plugin);
                            loaded.add(plugin.getName().toLowerCase());
                            String a = "";
                            int ai = 0;
                            for (String author : plugin.getAuthors()) {
                                ai++;
                                if (ai > 1) {
                                    if (plugin.getAuthors().size() > 2) a += ", ";
                                    else if (plugin.getAuthors().size() == 2) a += ' ';
                                    if (ai == plugin.getAuthors().size()) a += "and ";
                                }
                                a += author;
                            }
                            engine.getAppInfo().getLogger().info.println("Loaded " + plugin.getName() + " v" + plugin.getVersion().toString() + " by " + a);
                            i++;
                        } catch (Throwable e) {
                            plugin.setEnabled(false);
                            throw new InvocationTargetException(e, "Problem loading plugin: " + plugin.getName());
                        }
                    } catch (InvocationTargetException e) {
                        engine.getAppInfo().getLogger().error.println(e);
                        loaded.add(plugin.getName().toLowerCase());
                    }
                }
                progress = 0;
                for (String name : loaded) {
                    progress++;
                    plugins.remove(name);
                }
                if (progress == 0 && plugins.size() != 0) {
                    unstick++;
                    if (unstick > 3) {
                        engine.getAppInfo().getLogger().error.println(new IllegalStateException("Couldn't load more plugins yet " + plugins.size() + " remain unloaded"));
                        break;
                    }
                } else unstick = 0;
            }
            return i;
        } else return 0;
    }

    @Override
    public Map<String, PluginInfo> getPlugins() {
        return new LinkedHashMap<String, PluginInfo>(plugins);
    }
}
