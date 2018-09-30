package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Engine.Library.PluginClassLoader;
import net.ME1312.Galaxi.Library.Event.Cancellable;
import net.ME1312.Galaxi.Library.Event.Event;
import net.ME1312.Galaxi.Library.Event.EventHandler;
import net.ME1312.Galaxi.Library.Exception.IllegalPluginException;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.Command;
import net.ME1312.Galaxi.Plugin.Plugin;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Plugin Manager Class
 */
public class PluginManager implements net.ME1312.Galaxi.Plugin.PluginManager {
    private TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>> listeners = new TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>>();
    private TreeMap<String, Command> commands = new TreeMap<String, Command>();
    private HashMap<String, PluginInfo> plugins = new LinkedHashMap<String, PluginInfo>();
    private List<String> knownClasses = new ArrayList<String>();
    private GalaxiEngine engine;

    PluginManager(GalaxiEngine engine) {
        this.engine = engine;
    }

    /**
     * Search for classes
     */
    void findClasses(Class<?> clazz) {
        try {
            JarFile jarFile = new JarFile(new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()));
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                    knownClasses.add(entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.'));
                }
            }
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.println(e);
        }
    }

    /**
     * Load plugins
     *
     * @param directories Directories to search
     */
    public int loadPlugins(File... directories) {
        LinkedList<File> pljars = new LinkedList<File>();
        for (File directory : directories) {
            directory.mkdirs();
            if (directory.exists() && directory.isDirectory()) for (File file : directory.listFiles()) {
                if (file.getName().endsWith(".jar")) pljars.add(file);
            }
        }
        if (pljars.size() > 0) {
            long begin = Calendar.getInstance().getTime().getTime();

            /*
             * Load Jars & Find Main Classes
             * (Unordered)
             */
            LinkedHashMap<PluginClassLoader, NamedContainer<LinkedList<String>, LinkedHashMap<String, String>>> classes = new LinkedHashMap<PluginClassLoader, NamedContainer<LinkedList<String>, LinkedHashMap<String, String>>>();
            for (File file : pljars) {
                try {
                    JarFile jar = new JarFile(file);
                    Enumeration<JarEntry> entries = jar.entries();
                    PluginClassLoader loader = new PluginClassLoader(this.getClass().getClassLoader(), file);
                    List<String> contents = new ArrayList<String>();

                    loader.setDefaultClass(ClassNotFoundException.class);
                    boolean isplugin = false;
                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                            String cname = entry.getName().substring(0, entry.getName().length() - 6).replace('/', '.');
                            contents.add(cname);
                            try {
                                Class<?> clazz = loader.loadClass(cname);
                                if (clazz.isAnnotationPresent(Plugin.class)) {
                                    NamedContainer<LinkedList<String>, LinkedHashMap<String, String>> jarmap = (classes.keySet().contains(loader))?classes.get(loader):new NamedContainer<LinkedList<String>, LinkedHashMap<String, String>>(new LinkedList<String>(), new LinkedHashMap<>());
                                    for (String dependancy : clazz.getAnnotation(Plugin.class).dependencies()) jarmap.name().add(dependancy);
                                    for (String dependancy : clazz.getAnnotation(Plugin.class).softDependencies()) jarmap.name().add(dependancy);
                                    jarmap.get().put(clazz.getAnnotation(Plugin.class).name(), cname);
                                    classes.put(loader, jarmap);
                                    isplugin = true;
                                }
                            } catch (Throwable e) {
                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Couldn't load class: " + cname));
                            }
                        }
                    }
                    loader.setDefaultClass(null);

                    if (!isplugin) {
                        engine.getAppInfo().getLogger().info.println("Loaded Library: " + file.getName());
                    }
                    knownClasses.addAll(contents);
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
                    for (String name : classes.get(loader).get().keySet()) {
                        boolean load = true;
                        for (String depend : classes.get(loader).name()) {
                            if (!plugins.keySet().contains(depend.toLowerCase())) {
                                load = progress <= 0;
                            }
                        }

                        if (load) {
                            String main = classes.get(loader).get().get(name);
                            try {
                                Class<?> clazz = loader.loadClass(main);
                                if (!clazz.isAnnotationPresent(Plugin.class))
                                    throw new ClassCastException("Cannot find plugin descriptor");

                                Object obj = clazz.getConstructor().newInstance();
                                try {
                                    PluginInfo plugin = PluginInfo.getPluginInfo(obj);
                                    if (plugins.keySet().contains(plugin.getName().toLowerCase()))
                                        engine.getAppInfo().getLogger().warn.println("Duplicate plugin: " + plugin.getName());
                                    plugin.addExtra("galaxi.plugin.loadafter", new ArrayList<String>());
                                    if (loader.getFiles().length > 0 && loader.getFiles()[0] != null) {
                                        Field f = PluginInfo.class.getDeclaredField("dir");
                                        f.setAccessible(true);
                                        f.set(plugin, new File(loader.getFiles()[0].getParentFile(), plugin.getName()));
                                        f.setAccessible(false);
                                    }
                                    plugins.put(plugin.getName().toLowerCase(), plugin);
                                } catch (Throwable e) {
                                    engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Couldn't load plugin descriptor for main class: " + main));
                                }
                            } catch (ClassCastException e) {
                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e, "Main class isn't annotated as a SubPlugin: " + main));
                            } catch (InvocationTargetException e) {
                                engine.getAppInfo().getLogger().error.println(new IllegalPluginException(e.getTargetException(), "Uncaught exception occurred while loading main class: " + main));
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
                    NamedContainer<LinkedList<String>, LinkedHashMap<String, String>> jarmap = classes.get(loader);
                    progress++;
                    for (String main : loaded.get(loader)) jarmap.get().remove(main);
                    if (jarmap.get().size() > 0) {
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
                        for (String depend : plugin.getDependancies()) {
                            if (plugins.keySet().contains(depend.toLowerCase())) {
                                if (unstick != 2) {
                                    load = false;
                                } else {
                                    throw new IllegalPluginException(new IllegalStateException("Infinite dependency loop: " + plugin.getName() + " -> " + depend), "Cannot meet requirements for plugin: " + plugin.getName() + " v" + plugin.getVersion().toString());
                                }
                            } else if (!this.plugins.keySet().contains(depend.toLowerCase())) {
                                throw new IllegalPluginException(new IllegalStateException("Unknown dependency: " + depend), "Cannot meet requirements for plugin: " + plugin.getName() + " v" + plugin.getVersion().toString());
                            }
                        }
                        for (String softdepend : plugin.getSoftDependancies()) {
                            if (plugins.keySet().contains(softdepend.toLowerCase())) {
                                if (unstick != 3) {
                                    load = false;
                                } else {
                                    engine.getAppInfo().getLogger().warn.println(new IllegalStateException("Infinite soft dependency loop: " + plugin.getName() + " -> " + softdepend));
                                }
                            }
                        }
                        for (String loadafter : plugin.getExtra("galaxi.plugin.loadafter").asRawStringList()) {
                            if (plugins.keySet().contains(loadafter.toLowerCase())) {
                                if (unstick != 1) {
                                    load = false;
                                } else {
                                    engine.getAppInfo().getLogger().warn.println(new IllegalStateException("Infinite load before loop: " + loadafter + " -> " + plugin.getName()));
                                }
                            }
                        }
                        if (load) try {
                            plugin.removeExtra("galaxi.plugin.loadafter");
                            plugin.setEnabled(true);
                            registerListener(plugin, plugin.get());
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

    @Override
    public PluginInfo getPlugin(String name) {
        if (Util.isNull(name)) throw new NullPointerException();
        return getPlugins().get(name.toLowerCase());
    }

    @Override
    @SuppressWarnings("unchecked")
    public PluginInfo getPlugin(Class<?> main) {
        if (Util.isNull(main)) throw new NullPointerException();
        return Util.getDespiteException(() -> {
            Field f = PluginInfo.class.getDeclaredField("pluginMap");
            f.setAccessible(true);
            HashMap<Class<?>, PluginInfo> map = (HashMap<Class<?>, PluginInfo>) f.get(null);
            f.setAccessible(false);
            return map.get(main);
        }, null);
    }

    @Override
    public PluginInfo getPlugin(Object main) {
        if (Util.isNull(main)) throw new NullPointerException();
        return Util.getDespiteException(() -> PluginInfo.getPluginInfo(main), null);
    }

    @Override
    public void addCommand(Command command, String... handles) {
        for (String handle : handles) {
            commands.put(handle.toLowerCase(), command);
        }
    }

    @Override
    public void removeCommand(String... handles) {
        for (String handle : handles) {
            commands.remove(handle.toLowerCase());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void registerListener(PluginInfo plugin, Object... listeners) {
        for (Object listener : listeners) {
            if (Util.isNull(plugin, listener)) throw new NullPointerException();
            for (Method method : Arrays.asList(listener.getClass().getMethods())) {
                if (method.isAnnotationPresent(EventHandler.class)) {
                    if (method.getParameterTypes().length == 1) {
                        if (Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                            HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>> events = (this.listeners.keySet().contains(method.getAnnotation(EventHandler.class).order()))?this.listeners.get(method.getAnnotation(EventHandler.class).order()):new LinkedHashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>();
                            HashMap<PluginInfo, HashMap<Object, List<Method>>> plugins = (events.keySet().contains((Class<Event>) method.getParameterTypes()[0]))?events.get((Class<Event>) method.getParameterTypes()[0]):new LinkedHashMap<PluginInfo, HashMap<Object, List<Method>>>();
                            HashMap<Object, List<Method>> objects = (plugins.keySet().contains(plugin))?plugins.get(plugin):new LinkedHashMap<Object, List<Method>>();
                            List<Method> methods = (objects.keySet().contains(listener))?objects.get(listener):new LinkedList<Method>();
                            methods.add(method);
                            objects.put(listener, methods);
                            plugins.put(plugin, objects);
                            events.put((Class<Event>) method.getParameterTypes()[0], plugins);
                            this.listeners.put(method.getAnnotation(EventHandler.class).order(), events);
                        } else {
                            plugin.getLogger().error.println(
                                    "Cannot register listener \"" + listener.getClass().getCanonicalName() + '.' + method.getName() + "(" + method.getParameterTypes()[0].getCanonicalName() + ")\":",
                                    "\"" + method.getParameterTypes()[0].getCanonicalName() + "\" is not an Event");
                        }
                    } else {
                        LinkedList<String> args = new LinkedList<String>();
                        for (Class<?> clazz : method.getParameterTypes()) args.add(clazz.getCanonicalName());
                        plugin.getLogger().error.println(
                                "Cannot register listener \"" + listener.getClass().getCanonicalName() + '.' + method.getName() + "(" + args.toString().substring(1, args.toString().length() - 1) + ")\":",
                                ((method.getParameterTypes().length > 0) ? "Too many" : "No") + " parameters for method to be executed");
                    }
                }
            }
        }
    }

    @Override
    public void unregisterListener(PluginInfo plugin, Object... listeners) {
        for (Object listener : listeners) {
            if (Util.isNull(plugin, listener)) throw new NullPointerException();
            TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>> map = new TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>>(this.listeners);
            for (Short order : map.keySet()) {
                for (Class<? extends Event> event : map.get(order).keySet()) {
                    if (map.get(order).get(event).keySet().contains(plugin) && map.get(order).get(event).get(plugin).keySet().contains(listener)) {
                        HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>> events = this.listeners.get(order);
                        HashMap<PluginInfo, HashMap<Object, List<Method>>> plugins = this.listeners.get(order).get(event);
                        HashMap<Object, List<Method>> objects = this.listeners.get(order).get(event).get(plugin);
                        objects.remove(listener);
                        plugins.put(plugin, objects);
                        events.put(event, plugins);
                        this.listeners.put(order, events);
                    }
                }
            }
        }
    }

    @Override
    public void executeEvent(Event event) {
        if (Util.isNull(event)) throw new NullPointerException();
        for (Short order : listeners.keySet()) {
            if (listeners.get(order).keySet().contains(event.getClass())) {
                for (PluginInfo plugin : listeners.get(order).get(event.getClass()).keySet()) {
                    try {
                        Field pf = Event.class.getDeclaredField("plugin");
                        pf.setAccessible(true);
                        pf.set(event, plugin);
                        pf.setAccessible(false);
                    } catch (Exception e) {
                        engine.getAppInfo().getLogger().error.println(e);
                    }
                    for (Object listener : listeners.get(order).get(event.getClass()).get(plugin).keySet()) {
                        for (Method method : listeners.get(order).get(event.getClass()).get(plugin).get(listener)) {
                            if (!(event instanceof Cancellable) || !((Cancellable) event).isCancelled() || method.getAnnotation(EventHandler.class).override()) {
                                try {
                                    method.invoke(listener, event);
                                } catch (InvocationTargetException e) {
                                    plugin.getLogger().error.println("Event listener \"" + listener.getClass().getCanonicalName() + '.' + method.getName() + "(" + event.getClass().getTypeName() + ")\" had an unhandled exception:");
                                    plugin.getLogger().error.println(e.getTargetException());
                                } catch (IllegalAccessException e) {
                                    plugin.getLogger().error.println("Cannot access method \"" + listener.getClass().getCanonicalName() + '.' + method.getName() + "(" + event.getClass().getTypeName() + ")\"");
                                    plugin.getLogger().error.println(e);
                                }
                            }
                        }
                    }
                }
            }
        }
        try {
            Field pf = Event.class.getDeclaredField("plugin");
            pf.setAccessible(true);
            pf.set(event, null);
            pf.setAccessible(false);
        } catch (Exception e) {
           engine.getAppInfo().getLogger().error.println(e);
        }
    }
}
