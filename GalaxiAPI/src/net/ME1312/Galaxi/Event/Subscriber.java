package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.lang.reflect.Method;

/**
 * Event Subscriber Information Class
 */
public class Subscriber {

    /**
     * The event being listened for
     */
    public final Class<? extends Event> event;

    /**
     * The order of this subscriber
     * @see ListenerOrder ListenerOrder
     */
    public final short order;

    /**
     * The plugin this subscriber belongs to
     */
    public final PluginInfo plugin;

    /**
     * The object this subscriber belongs to
     */
    public final Object listener;

    /**
     * The method this subscriber calls
     */
    public final Method method;

    /**
     * Whether this subscriber overrides other subscribers
     * @see Subscribe#override() @Subscribe
     */
    public final boolean override;

    protected Subscriber(Class<? extends Event> event, short order, PluginInfo plugin, Object listener, Method method, boolean override) {
        Util.nullpo(event, plugin, listener, method);
        this.event = event;
        this.order = order;
        this.plugin = plugin;
        this.listener = listener;
        this.method = method;
        this.override = override;
    }
}
