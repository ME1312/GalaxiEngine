package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Event.*;
import net.ME1312.Galaxi.Library.Event.Listener;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.Command.Command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class PluginManager {
    private TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>> listeners = new TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>>();
    private TreeMap<String, Command> commands = new TreeMap<String, Command>();

    /**
     * Get a map of the Plugins
     *
     * @return PluginInfo Map
     */
    public abstract Map<String, PluginInfo> getPlugins();

    /**
     * Gets a Plugin
     *
     * @param name Plugin Name
     * @return PluginInfo
     */
    public PluginInfo getPlugin(String name) {
        if (Util.isNull(name)) throw new NullPointerException();
        return getPlugins().get(name.toLowerCase());
    }

    /**
     * Gets a Plugin
     *
     * @param main Plugin Main Class
     * @return PluginInfo
     */
    @SuppressWarnings("unchecked")
    public PluginInfo getPlugin(Class<?> main) {
        if (Util.isNull(main)) throw new NullPointerException();
        return Util.getDespiteException(() -> Util.reflect(PluginInfo.class.getDeclaredField("pluginMap"), null), null);
    }

    /**
     * Gets a Plugin
     *
     * @param main Plugin Object
     * @return PluginInfo
     */
    public PluginInfo getPlugin(Object main) {
        if (Util.isNull(main)) throw new NullPointerException();
        return Util.getDespiteException(() -> PluginInfo.getPluginInfo(main), null);
    }

    /**
     * Registers a Command
     *
     * @param command Command
     * @param handles Aliases
     */
    public void addCommand(Command command, String... handles) {
        for (String handle : handles) {
            commands.put(handle.toLowerCase(), command);
        }
    }

    /**
     * Unregisters a Command
     *
     * @param handles Aliases
     */
    public void removeCommand(String... handles) {
        for (String handle : handles) {
            commands.remove(handle.toLowerCase());
        }
    }

    /**
     * Register SubEvent Listeners
     *
     * @see Subscribe
     * @param plugin PluginInfo
     * @param listeners Listeners
     */
    @SuppressWarnings("unchecked")
    public void registerListeners(PluginInfo plugin, Object... listeners) {
        for (Object listener : listeners) {
            if (Util.isNull(plugin, listener)) throw new NullPointerException();
            for (Method method : Arrays.asList(listener.getClass().getMethods())) {
                if (method.isAnnotationPresent(Subscribe.class)) {
                    if (method.getParameterTypes().length == 1) {
                        if (Event.class.isAssignableFrom(method.getParameterTypes()[0])) {
                            registerListener(plugin, (Class<? extends Event>) method.getParameterTypes()[0], method.getAnnotation(Subscribe.class).order(), listener, method);
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

    /**
     * Register SubEvent Listeners
     *
     * @param plugin PluginInfo
     * @param event Event Type
     * @param listeners Listeners
     * @param <T> Event Type
     */
    @SafeVarargs
    public final <T extends Event> void registerListener(PluginInfo plugin, Class<T> event, Listener<T>... listeners) {
        registerListener(plugin, event, null, listeners);
    }


    /**
     * Register SubEvent Listeners
     *
     * @see ListenerOrder
     * @param plugin PluginInfo
     * @param event Event Type
     * @param order Listener Order (will convert to short)
     * @param listeners Listeners
     * @param <T> Event Type
     */
    @SafeVarargs
    public final <T extends Event> void registerListener(PluginInfo plugin, Class<T> event, Number order, Listener<T>... listeners) {
        for (Listener listener : listeners) {
            if (Util.isNull(plugin, event, listener)) throw new NullPointerException();
            try {
                short o;
                Method m = Listener.class.getMethod("run", Event.class);
                if (order == null) {
                    if (m.isAnnotationPresent(Subscribe.class)) {
                        o = m.getAnnotation(Subscribe.class).order();
                    } else o = ListenerOrder.NORMAL;
                } else o = order.shortValue();
                registerListener(plugin, event, o, listener, m);
            } catch (Exception e) {
                Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
            }
        }
    }

    private void registerListener(PluginInfo plugin, Class<? extends Event> event, short order, Object listener, Method method) {
        HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>> events = (this.listeners.keySet().contains(order))?this.listeners.get(order):new LinkedHashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>();
        HashMap<PluginInfo, HashMap<Object, List<Method>>> plugins = (events.keySet().contains(event))?events.get(event):new LinkedHashMap<PluginInfo, HashMap<Object, List<Method>>>();
        HashMap<Object, List<Method>> objects = (plugins.keySet().contains(plugin))?plugins.get(plugin):new LinkedHashMap<Object, List<Method>>();
        List<Method> methods = (objects.keySet().contains(listener))?objects.get(listener):new LinkedList<Method>();
        methods.add(method);
        objects.put(listener, methods);
        plugins.put(plugin, objects);
        events.put(event, plugins);
        this.listeners.put(order, events);
    }

    /**
     * Unregister SubEvent Listeners
     *
     * @param plugin PluginInfo
     * @param listeners Listeners
     */
    public void unregisterListeners(PluginInfo plugin, Object... listeners) {
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

    /**
     * Run a SubEvent
     *
     * @param event SubEvent
     */
    @SuppressWarnings("unchecked")
    public void executeEvent(Event event) {
        executeEvent((Class<Event>) event.getClass(), event);
    }

    /**
     * Run a SubEvent (as super class)
     *
     * @param type Super Class
     * @param event SubEvent
     * @param <T> Event Type
     */
    public <T extends Event> void executeEvent(Class<T> type, T event) {
        if (Util.isNull(event)) throw new NullPointerException();
        if (!type.isInstance(event)) throw new ClassCastException(event.getClass().getCanonicalName() + " cannot be cast to " + type.getCanonicalName());
        boolean reverse = type.isAnnotationPresent(ReverseOrder.class);

        LinkedList<Short> o = new LinkedList<>(listeners.keySet());
        if (reverse) Collections.reverse(o);
        for (Short order : listeners.keySet()) {
            if (listeners.get(order).keySet().contains(type)) {
                LinkedList<PluginInfo> p = new LinkedList<>(listeners.get(order).get(type).keySet());
                if (reverse) Collections.reverse(p);
                for (PluginInfo plugin : p) {
                    try {
                        Util.reflect(Event.class.getDeclaredField("plugin"), event, plugin);
                    } catch (Exception e) {
                        Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
                    }
                    LinkedList<Object> l = new LinkedList<>(listeners.get(order).get(type).get(plugin).keySet());
                    if (reverse) Collections.reverse(l);
                    for (Object listener : l) {
                        LinkedList<Method> m = new LinkedList<>(listeners.get(order).get(type).get(plugin).get(listener));
                        if (reverse) Collections.reverse(m);
                        for (Method method : m) {
                            if (!(event instanceof Cancellable) || !((Cancellable) event).isCancelled() || (method.isAnnotationPresent(Subscribe.class) && method.getAnnotation(Subscribe.class).override())) {
                                try {
                                    method.invoke(listener, event);
                                } catch (InvocationTargetException e) {
                                    plugin.getLogger().error.println("Event listener \"" + listener.getClass().getCanonicalName() + '.' + method.getName() + "(" + type.getTypeName() + ")\" had an unhandled exception:");
                                    plugin.getLogger().error.println(e.getTargetException());
                                } catch (IllegalAccessException e) {
                                    plugin.getLogger().error.println("Cannot access method \"" + listener.getClass().getCanonicalName() + '.' + method.getName() + "(" + type.getTypeName() + ")\"");
                                    plugin.getLogger().error.println(e);
                                }
                            }
                        }
                    }
                }
            }
        }
        try {
            Util.reflect(Event.class.getDeclaredField("plugin"), event, null);
        } catch (Exception e) {
            Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
        }
    }
}
