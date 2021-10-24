package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Command.Command;
import net.ME1312.Galaxi.Event.*;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Library.Try;
import net.ME1312.Galaxi.Library.Util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class PluginManager {
    private final Map<Class<? extends Event>, Map<Short, Map<PluginInfo, Map<Object, List<BakedListener>>>>> listeners = new HashMap<>();
    private final Map<Class<? extends Event>, List<BakedListener>> baked = new HashMap<>();
    protected final TreeMap<String, Command> commands = new TreeMap<String, Command>();

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
        Util.nullpo(name);
        return getPlugins().get(name.toLowerCase());
    }

    /**
     * Gets a Plugin
     *
     * @param main Plugin Main Class
     * @return PluginInfo
     */
    public PluginInfo getPlugin(Class<?> main) {
        Util.nullpo(main);
        return PluginInfo.get(main);
    }

    /**
     * Gets a Plugin
     *
     * @param main Plugin Object
     * @return PluginInfo
     */
    public PluginInfo getPlugin(Object main) {
        Util.nullpo(main);
        return PluginInfo.get(main);
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
     * Register Event Listeners
     *
     * @see Subscribe
     * @param plugin Plugin
     * @param listeners Listeners
     */
    @SuppressWarnings("unchecked")
    public void registerListeners(PluginInfo plugin, Object... listeners) {
        Util.nullpo(plugin);
        Util.nullpo(listeners);
        synchronized (this.listeners) {
            LinkedList<Class<? extends Event>> update = new LinkedList<>();
            for (Object listener : listeners) {
                for (Method method : listener.getClass().getMethods()) {
                    if (method.isAnnotationPresent(Subscribe.class)) {
                        if (method.getParameterTypes().length == 1) {
                            Class<?> event = method.getParameterTypes()[0];
                            if (Event.class.isAssignableFrom(event)) {
                                registerListener((Class<? extends Event>) event, method.getAnnotation(Subscribe.class).order(), plugin, listener, method, method.getAnnotation(Subscribe.class).override());
                                if (!update.contains(event)) update.add((Class<? extends Event>) event);
                            } else {
                                plugin.getLogger().error.println(
                                        "Cannot register listener \"" + listener.getClass().getCanonicalName() + '.' + method.getName() + "(" + event.getCanonicalName() + ")\":",
                                        "\"" + event.getCanonicalName() + "\" is not an Event");
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

            bakeEvents(update);
        }
    }

    /**
     * Register Event Listeners
     *
     * @param plugin Plugin
     * @param event Event Type
     * @param listeners Listeners
     * @param <T> Event Type
     */
    @SafeVarargs
    public final <T extends Event> void registerListener(PluginInfo plugin, Class<T> event, Listener<? extends T>... listeners) {
        registerListener(plugin, event, null, listeners);
    }


    /**
     * Register Event Listeners
     *
     * @see ListenerOrder
     * @param plugin Plugin
     * @param event Event Type
     * @param order Listener Order (will convert to short)
     * @param listeners Listeners
     * @param <T> Event Type
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @SafeVarargs
    public final <T extends Event> void registerListener(PluginInfo plugin, Class<T> event, Number order, Listener<? extends T>... listeners) {
        Util.nullpo(plugin, event, listeners);
        Util.nullpo((Object[]) listeners);
        synchronized (this.listeners) {
            for (Listener listener : listeners) {
                try {
                    short o;
                    Method m = Listener.class.getMethod("run", Event.class);
                    if (order == null) {
                        if (m.isAnnotationPresent(Subscribe.class)) {
                            o = m.getAnnotation(Subscribe.class).order();
                        } else o = ListenerOrder.NORMAL;
                    } else o = order.shortValue();

                    registerListener(event, o, plugin, listener, null, m.isAnnotationPresent(Subscribe.class) && m.getAnnotation(Subscribe.class).override());
                } catch (NoSuchMethodException e) {
                    Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
                }
            }

            bakeEvent(event);
        }
    }

    private void registerListener(Class<? extends Event> event, short order, PluginInfo plugin, Object listener, Method method, boolean override) {
        Map<Short, Map<PluginInfo, Map<Object, List<BakedListener>>>> orders = this.listeners.getOrDefault(event, new TreeMap<>());
        Map<PluginInfo, Map<Object, List<BakedListener>>> plugins = orders.getOrDefault(order, new LinkedHashMap<>());
        Map<Object, List<BakedListener>> listeners = plugins.getOrDefault(plugin, new LinkedHashMap<>());
        List<BakedListener> actions = listeners.getOrDefault(listener, new LinkedList<>());

        actions.add(new BakedListener(event, order, plugin, listener, method, override));
        listeners.put(listener, actions);
        plugins.put(plugin, listeners);
        orders.put(order, plugins);
        this.listeners.put(event, orders);
    }

    /**
     * Unregister Event Listeners
     *
     * @param plugin Plugin
     */
    public void unregisterListeners(PluginInfo plugin) {
        unregisterListeners(plugin, (Object[]) null);
    }

    /**
     * Unregister Event Listeners
     *
     * @param plugin Plugin
     * @param listeners Listeners
     */
    public void unregisterListeners(PluginInfo plugin, Object... listeners) {
        Util.nullpo(plugin, listeners);
        Util.nullpo(listeners);
        synchronized (this.listeners) {
            LinkedList<Class<? extends Event>> update = new LinkedList<>();
            for (Map.Entry<Class<? extends Event>, Map<Short, Map<PluginInfo, Map<Object, List<BakedListener>>>>> event : this.listeners.entrySet()) {
                for (Map<PluginInfo, Map<Object, List<BakedListener>>> plugins : event.getValue().values()) {
                    if (plugins.containsKey(plugin)) {
                        if (listeners == null) {
                            plugins.remove(plugin);
                            if (!update.contains(event.getKey())) update.add(event.getKey());
                        } else {
                            Map<Object, List<BakedListener>> map = plugins.get(plugin);
                            for (Object listener : listeners) {
                                if (map.containsKey(listener)) {
                                    map.remove(listener);
                                    if (!update.contains(event.getKey())) update.add(event.getKey());
                                }
                            }
                        }
                    }
                }
            }

            bakeEvents(update);
        }
    }

    private static final class BakedListener {
        private final Class<? extends Event> event;
        private final short order;
        private final PluginInfo plugin;
        private final Object listener;
        private final Method method;
        private final boolean override;

        private BakedListener(Class<? extends Event> event, short order, PluginInfo plugin, Object listener, Method method, boolean override) {
            this.event = event;
            this.order = order;
            this.plugin = plugin;
            this.listener = listener;
            this.method = method;
            this.override = override;
        }
    }

    private void bakeEvents(List<Class<? extends Event>> events) {
        for (Class<? extends Event> event : events) {
            bakeEvent(event);
        }
    }

    private void bakeEvent(Class<? extends Event> event) {
        LinkedList<BakedListener> baked = new LinkedList<BakedListener>();
        boolean reverse = event.isAnnotationPresent(ReverseOrder.class);
        if (this.listeners.containsKey(event)) {
            for (Map.Entry<Short, Map<PluginInfo, Map<Object, List<BakedListener>>>> order : this.listeners.get(event).entrySet()) {
                for (Map.Entry<PluginInfo, Map<Object, List<BakedListener>>> plugin : order.getValue().entrySet()) {
                    for (Map.Entry<Object, List<BakedListener>> listener : plugin.getValue().entrySet()) {
                        for (BakedListener action : listener.getValue()) {
                            if (reverse) {
                                baked.addFirst(action);
                            } else {
                                baked.addLast(action);
                            }
                        }
                    }
                }
            }
            this.baked.put(event, baked);
        } else {
            this.baked.remove(event);
        }
    }

    /**
     * Run an Event
     *
     * @param event SubEvent
     */
    @SuppressWarnings("unchecked")
    public void executeEvent(Event event) {
        executeEvent((Class<Event>) event.getClass(), event);
    }

    /**
     * Run an Event (as super class)
     *
     * @param type Super Class
     * @param event SubEvent
     * @param <T> Event Type
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void executeEvent(Class<T> type, T event) {
        Util.nullpo(event);
        if (!type.isInstance(event)) throw new ClassCastException(event.getClass().getCanonicalName() + " cannot be cast to " + type.getCanonicalName());
        List<BakedListener> listeners = this.baked.get(type);

        if (listeners != null && listeners.size() != 0) {
            Container<PluginInfo> plugin = Try.all.getOrSupply(() -> Util.reflect(Event.class.getDeclaredField("plugin"), event), Container::new);
            for (BakedListener listener : listeners) {
                plugin.value = listener.plugin;
                if (!(event instanceof Cancellable) || !((Cancellable) event).isCancelled() || listener.override) {
                    if (listener.method != null) {
                        try {
                            listener.method.invoke(listener.listener, event);
                        } catch (IllegalAccessException e) {
                            listener.plugin.getLogger().error.println("Cannot access method: \"" + listener.getClass().getCanonicalName() + '.' + listener.method.getName() + "(" + type.getTypeName() + ")\"");
                            listener.plugin.getLogger().error.println(e);
                        } catch (InvocationTargetException e) {
                            listener.plugin.getLogger().error.println("Event listener for \"" + type.getTypeName() + "\" had an unhandled exception:");
                            listener.plugin.getLogger().error.println(e.getTargetException());
                        }
                    } else if (listener.listener instanceof Listener) {
                        try {
                            ((Listener<T>) listener.listener).run(event);
                        } catch (Throwable e) {
                            listener.plugin.getLogger().error.println("Event listener for \"" + type.getTypeName() + "\" had an unhandled exception:");
                            listener.plugin.getLogger().error.println(e);
                        }
                    }
                }
            }
            plugin.value = null;
        }
    }
}
