package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Event.Event;
import net.ME1312.Galaxi.Event.ReverseOrder;
import net.ME1312.Galaxi.Library.Try;
import net.ME1312.Galaxi.Library.Util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;

final class EventManager {
    static final MethodHandle PLUGIN_REFERENCE = Try.none.get(() -> {
        Field f = Event.class.getDeclaredField("plugin");
        f.setAccessible(true);
        return MethodHandles.lookup().unreflectGetter(f);
    });
    private Map<Short, Map<PluginInfo, Map<Object, List<EventSubscription>>>> map;
    private final ArrayList<EventManager> children = new ArrayList<>();
    private boolean unique, update;
    final EventManager parent;
    final Class<? extends Event> event;
    final boolean reversed;
    EventSnapshot snapshot;

    @SuppressWarnings("unchecked")
    static EventManager get(Map<Class<? extends Event>, EventManager> map, Class<? extends Event> event) {
        EventManager value = map.get(event);
        if (value != null) return value;
        if (Event.class.isAssignableFrom(event)) {
            value = new EventManager(get(map, (Class<? extends Event>) event.getSuperclass()), event);
            map.put(event, value);
        }
        return value;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    private EventManager(EventManager parent, Class<? extends Event> event) {
        this.parent = parent;
        this.event = event;

        ReverseOrder annotation = event.getAnnotation(ReverseOrder.class);
        this.reversed = annotation != null && annotation.value();

        if (parent != null) synchronized (parent) {
            parent.children.add(this);
            snapshot = parent.snapshot;
        } else {
            snapshot = EventSnapshot.EMPTY;
        }
    }

    private synchronized void synchronize(Consumer<EventManager> action) {
        if (unique || parent == null) {
            action.accept(this);
        } else {
            parent.synchronize(action);
        }
    }

    private synchronized void diverge() {
        if (!unique) {
            map = new TreeMap<>();
            if (parent != null) {
                boolean update = this.update;
                parent.synchronize(parent -> {
                    parent.bake();
                    unique = true;
                    for (EventSubscription data : snapshot.normal) {
                        add(data);
                    }
                });
                this.update = update;
            } else {
                this.unique = true;
            }
        }
    }

    synchronized EventManager register(EventSubscription sub) {
        Util.nullpo(sub);
        diverge();
        add(sub);
        return this;
    }

    private void add(EventSubscription sub) {
        if (unique) synchronized (this) {
            update = true;
            map.computeIfAbsent(sub.order, k -> new LinkedHashMap<>())
                    .computeIfAbsent(sub.plugin, k -> new LinkedHashMap<>())
                    .computeIfAbsent(sub.listener, k -> new LinkedList<>())
                    .add(sub);
        }
        for (EventManager child : children) child.add(sub);
    }

    synchronized EventManager unregister(PluginInfo plugin, Object[] listeners) {
        Util.nullpo(plugin);
        diverge();
        remove(plugin, listeners);
        return this;
    }

    private void remove(PluginInfo plugin, Object[] listeners) {
        if (unique) synchronized (this) {
            map.values().removeIf(plugins -> {
                if (plugins.containsKey(plugin)) {
                    if (listeners == null) {
                        update = true;
                        plugins.remove(plugin);
                    } else {
                        Map<Object, List<EventSubscription>> subs = plugins.get(plugin);
                        for (Object listener : listeners) {
                            if (subs.containsKey(listener)) {
                                update = true;
                                subs.remove(listener);
                            }
                        }
                        if (subs.isEmpty()) {
                            plugins.remove(plugin);
                        }
                    }
                }
                return plugins.isEmpty();
            });
            if (map.isEmpty()) {
                unique = false;
            }
        }
        for (EventManager child : children) child.remove(plugin, listeners);
    }

    void bake() {
        if (unique) {
            if (!update) return;
            synchronized (this) {
                int iNormal = 0;
                int iOverride = 0;
                List<EventSubscription> normal = new LinkedList<>();
                List<EventSubscription> override = new LinkedList<>();
                for (Map<PluginInfo, Map<Object, List<EventSubscription>>> plugins : map.values()) {
                    for (Map<Object, List<EventSubscription>> listeners : plugins.values()) {
                        for (List<EventSubscription> subs : listeners.values()) {
                            for (EventSubscription sub : subs) {
                                normal.add(sub = sub.copy(iNormal++, iOverride));
                                if (sub.override) {
                                    override.add(sub);
                                    ++iOverride;
                                }
                            }
                        }
                    }
                }
                snapshot = new EventSnapshot(
                        normal.toArray(EventSnapshot.EMPTY_ARRAY),
                        override.toArray(EventSnapshot.EMPTY_ARRAY)
                );
                update = false;
            }
        } else {
            snapshot = (parent == null)? EventSnapshot.EMPTY : parent.snapshot;
        }
        for (EventManager child : children) child.bake();
    }
}
