package net.ME1312.Galaxi.Library.Event;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * SubEvent Layout Class
 */
public abstract class Event {
    private PluginInfo plugin = null;

    /**
     * Get the Galaxi Engine API
     *
     * @return Get the Galaxi Engine API
     */
    public Galaxi getEngine() {
        return Galaxi.getInstance();
    }

    /**
     * Gets your Plugin's Info
     *
     * @return Plugin Info
     */
    public PluginInfo getPlugin() {
        return plugin;
    }

    /**
     * Get the handlers for this event
     *
     * @return Handler Map
     */
    @SuppressWarnings({"deprecation", "unchecked"})
    public Map<PluginInfo, List<Method>> getHandlers() throws IllegalAccessException {
        try {
            Field f = Class.forName("net.ME1312.Galaxi.Engine.PluginManager").getDeclaredField("listeners");
            f.setAccessible(true);
            TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>> listeners = (TreeMap<Short, HashMap<Class<? extends Event>, HashMap<PluginInfo, HashMap<Object, List<Method>>>>>) f.get(getEngine());
            HashMap<PluginInfo, List<Method>> map = new LinkedHashMap<PluginInfo, List<Method>>();
            f.setAccessible(false);
            for (Short order : listeners.keySet()) {
                if (!listeners.get(order).keySet().contains(getClass())) continue;
                for (PluginInfo plugin : listeners.get(order).get(getClass()).keySet()) {
                    for (Object listener : listeners.get(order).get(getClass()).get(plugin).keySet()) {
                        for (Method method : listeners.get(order).get(getClass()).get(plugin).get(listener)) {
                            List<Method> methods = (map.keySet().contains(plugin))?map.get(plugin):new LinkedList<Method>();
                            methods.add(method);
                            map.put(plugin, methods);
                        }
                    }
                }
            }
            return map;
        } catch (Exception e) {
            getEngine().getAppInfo().getLogger().error.println(new InvocationTargetException(e, "Couldn't get handler list for event: " + toString()));
            return null;
        }
    }

    @Override
    public String toString() {
        return getClass().getTypeName();
    }
}
