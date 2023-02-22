package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Event.Cancellable;
import net.ME1312.Galaxi.Event.Event;
import net.ME1312.Galaxi.Event.Listener;
import net.ME1312.Galaxi.Event.Subscriber;
import net.ME1312.Galaxi.Library.Container.Container;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;

final class EventSubscription extends Subscriber {
    static final MethodType HANDLE_TYPE = MethodType.methodType(void.class, Event.class);
    final MethodHandle handle;
    final int iNormal, iOverride;

    EventSubscription(int iNormal, int iOverride, Class<? extends Event> event, short order, PluginInfo plugin, Object listener, Method method, MethodHandle handle, boolean override) {
        super(event, order, plugin, listener, method, override);
        this.handle = handle;
        this.iNormal = iNormal;
        this.iOverride = iOverride;
    }

    EventSubscription copy(int iNormal, int iOverride) {
        return new EventSubscription(iNormal, iOverride, event, order, plugin, listener, method, handle, override);
    }

    @SuppressWarnings("unchecked")
    boolean execute(Container<PluginInfo> plugin, Cancellable cancel, Event event) {
        plugin.value = this.plugin;
        try {
            if (handle != null) {
                handle.invokeExact((Event) event);
            } else {
                ((Listener<Event>) listener).run(event);
            }
        } catch (Throwable e) {
            this.plugin.getLogger().error.println("Event listener for \"" + this.event.getTypeName() + "\" had an unhandled exception:");
            this.plugin.getLogger().error.println(e);
        }
        return cancel.isCancelled();
    }
}
