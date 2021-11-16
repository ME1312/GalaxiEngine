package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Plugin.PluginInfo;

/**
 * SubEvent Layout Class
 */
public abstract class Event {
    private final Container<PluginInfo> plugin = new Container<>();

    /**
     * Get the GalaxiEngine API
     *
     * @return Get the GalaxiEngine API
     */
    public final Galaxi getEngine() {
        return Galaxi.getInstance();
    }

    /**
     * Gets your Plugin's Info
     *
     * @return Plugin Info
     */
    public final PluginInfo getPlugin() {
        return plugin.value;
    }
}
