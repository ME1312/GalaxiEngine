package net.ME1312.Galaxi.Library.Event;

import net.ME1312.Galaxi.Library.Callback;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.lang.reflect.Method;

/**
 * Event Listener Layout Class<br>
 * Classes implementing this and registered will call to {@link #run(Event)} when the event is run
 *
 * @see net.ME1312.Galaxi.Plugin.PluginManager#registerListener(PluginInfo, Class, Listener[])
 * @see net.ME1312.Galaxi.Plugin.PluginManager#registerListener(PluginInfo, Class, Number, Listener[])
 * @param <T> Event Type
 */
public interface Listener<T extends Event> extends Callback<T> {

    /**
     * Listen for an Event
     *
     * @param event Event
     */
    void run(T event);
}
