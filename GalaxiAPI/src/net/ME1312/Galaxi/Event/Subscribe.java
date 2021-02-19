package net.ME1312.Galaxi.Event;

import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Event Listener Method Annotation<br>
 * Methods annotated by this and registered will be called when the event is run
 *
 * @see net.ME1312.Galaxi.Plugin.PluginManager#registerListeners(PluginInfo, Object...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Subscribe {
    /**
     * Defines when this method should run in the stack
     *
     * @see ListenerOrder
     * @return Listener Order
     */
    short order() default ListenerOrder.NORMAL;

    /**
     * If this method should be called even after the event has been cancelled
     *
     * @return Override Status
     */
    boolean override() default false;
}
