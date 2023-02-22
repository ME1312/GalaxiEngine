package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Command.Command;
import net.ME1312.Galaxi.Event.*;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Library.Try;
import net.ME1312.Galaxi.Library.Util;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.*;

public abstract class PluginManager {
    private final Map<Class<? extends Event>, EventManager> events = new HashMap<>();
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
    public abstract PluginInfo getPlugin(String name);

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
     * @param plugin    Plugin
     * @param listeners Listeners
     * @see Subscribe @Subscribe
     */
    @SuppressWarnings("unchecked")
    public void registerListeners(PluginInfo plugin, Object... listeners) {
        Util.nullpo(plugin);
        Util.nullpo(listeners);
        List<EventManager> managers = new ArrayList<>();
        for (Object listener : listeners) {
            for (Method method : listener.getClass().getMethods()) {
                Subscribe annotation = method.getAnnotation(Subscribe.class);
                if (annotation != null) {
                    if (method.getParameterTypes().length == 1) {
                        Class<?> event = method.getParameterTypes()[0];
                        if (Event.class.isAssignableFrom(event)) {
                            EventManager manager = EventManager.get(events, (Class<? extends Event>) event);
                            try {
                                manager.register(new EventSubscription(-1, -1,
                                        (Class<? extends Event>) event,
                                        annotation.order(),
                                        plugin,
                                        listener,
                                        method, MethodHandles.publicLookup().unreflect(method).bindTo(listener).asType(EventSubscription.HANDLE_TYPE),
                                        annotation.override()
                                ));
                                managers.add(manager);
                            } catch (IllegalAccessException e) {
                                plugin.getLogger().error.println(e);
                            }
                        } else {
                            plugin.getLogger().error.println(
                                    "Cannot register listener \"" + listener.getClass().getTypeName() + '.' + method.getName() + "(" + event.getTypeName() + ")\":",
                                    "\"" + event.getTypeName() + "\" is not an Event");
                        }
                    } else {
                        String args = Arrays.toString(method.getParameterTypes());
                        plugin.getLogger().error.println(
                                "Cannot register listener \"" + listener.getClass().getTypeName() + '.' + method.getName() + "(" + args.substring(1, args.length() - 1) + ")\":",
                                ((method.getParameterTypes().length > 0)? "Too many" : "No") + " parameters for method to be executed");
                    }
                }
            }
        }
        for (Iterator<EventManager> i = managers.iterator(); i.hasNext(); i.next().bake());
    }

    /**
     * Register Event Listeners
     *
     * @param plugin    Plugin
     * @param event     Event Type
     * @param listeners Listeners
     * @param <T>       Event Type
     */
    @SafeVarargs
    public final <T extends Event> void registerListener(PluginInfo plugin, Class<T> event, Listener<? extends T>... listeners) {
        registerListener(plugin, event, null, listeners);
    }


    /**
     * Register Event Listeners
     *
     * @param plugin    Plugin
     * @param event     Event Type
     * @param order     Listener Order (will convert to short)
     * @param listeners Listeners
     * @param <T>       Event Type
     * @see ListenerOrder
     */
    @SafeVarargs
    public final <T extends Event> void registerListener(PluginInfo plugin, Class<T> event, Number order, Listener<? extends T>... listeners) {
        Util.nullpo(plugin, event);
        Util.nullpo(listeners);
        EventManager manager = EventManager.get(events, event);
        for (int i = 0; i < listeners.length; ++i) {
            try {
                Method method = Listener.class.getMethod("run", Event.class);
                Subscribe annotation = method.getAnnotation(Subscribe.class);

                manager.register(new EventSubscription(-1, -1,
                        event,
                        (order == null)? ((annotation == null)? ListenerOrder.NORMAL : annotation.order()) : order.shortValue(),
                        plugin,
                        listeners[i],
                        method, null,
                        annotation == null || annotation.override()
                ));
            } catch (NoSuchMethodException e) {
                Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
            }
        }
        manager.bake();
    }

    /**
     * Get Registered Event Listeners
     *
     * @param type Event Type
     * @return Event Listeners
     */
    public List<Subscriber> getListeners(Class<? extends Event> type) {
        EventManager manager = events.get(type);
        return (manager == null)? Collections.emptyList() : Collections.unmodifiableList(Arrays.asList(manager.snapshot.normal));
    }

    /**
     * Unregister Event Listeners
     *
     * @param plugin Plugin
     */
    public void unregisterListeners(PluginInfo plugin) {
        EventManager.get(events, Event.class).unregister(plugin, null).bake();
    }

    /**
     * Unregister Event Listeners
     *
     * @param plugin    Plugin
     * @param listeners Listeners
     */
    public void unregisterListeners(PluginInfo plugin, Object... listeners) {
        Util.nullpo(listeners);
        EventManager.get(events, Event.class).unregister(plugin, listeners).bake();
    }

    /**
     * Run an Event
     *
     * @param event Event
     */
    @SuppressWarnings("unchecked")
    public boolean executeEvent(Event event) {
        return executeEvent((Class<Event>) event.getClass(), event);
    }

    /**
     * Run an Event (as its super class)
     *
     * @param type  Super Class
     * @param event Event
     * @param <T>   Event Type
     * @return <i>true</i> if the event was not cancelled
     */
    @SuppressWarnings({"unchecked", "AssignmentUsedAsCondition"})
    public <T extends Event> boolean executeEvent(Class<T> type, T event) {
        if (Util.nullpo(type) == Event.class) throw new IllegalArgumentException("Cannot execute the base event class");
        if (!type.isInstance(event)) throw new ClassCastException(event.getClass().getTypeName() + '@' + Integer.toHexString(event.hashCode()) + " is not an instance of " + type.getTypeName());
        EventManager manager = events.get(type);

        if (manager != null) {
            Cancellable cancel = (event instanceof Cancellable)? ((Cancellable) event) : new Cancellable() {
                @Override
                public boolean isCancelled() {
                    return false;
                }
            };
            boolean normal;
            EventSnapshot snapshot = manager.snapshot;
            EventSubscription[] current = (normal = !cancel.isCancelled())? snapshot.normal : snapshot.overrides;

            if (current.length != 0) {
                Container<PluginInfo> plugin = Try.none.get(() -> (Container<PluginInfo>) EventManager.PLUGIN_REFERENCE.invokeExact((Event) event));

                if (manager.reversed) {
                    for (int i = current.length; i > 0;) {
                        if (cancel.isCancelled() != current[--i].execute(plugin, cancel, event)) {
                            if (!(normal = !normal)) { // if (normal == true)
                                i = current[i].iOverride;
                                current = snapshot.overrides;
                            } else {
                                i = current[i].iNormal;
                                current = snapshot.normal;
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < current.length; ++i) {
                        if (cancel.isCancelled() != current[i].execute(plugin, cancel, event)) {
                            if (!(normal = !normal)) { // if (normal == true)
                                i = current[i].iOverride;
                                current = snapshot.overrides;
                            } else {
                                i = current[i].iNormal;
                                current = snapshot.normal;
                            }
                        }
                    }
                }
                plugin.value = null;
                return normal;
            }
        }
        return true;
    }

}
