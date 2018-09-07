package net.ME1312.Galaxi.Library.Event;

import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * EventHandler Method Annotation<br>
 * Methods annotated by this and registered will be run when the event is called
 *
 * @see net.ME1312.Galaxi.Plugin.PluginManager#registerListener(PluginInfo, Object...)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EventHandler {
    /**
     * Defines when this event should run
     *
     * @see EventOrder
     * @return Event Order
     */
    short order() default EventOrder.NORMAL;

    /**
     * If this Event should be run even if it's been cancelled
     *
     * @return Override Status
     */
    boolean override() default false;
}
