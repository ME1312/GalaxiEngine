package net.ME1312.Galaxi.Event;

import java.lang.annotation.*;

/**
 * Reverse Order Event Annotation<br>
 * Events annotated by this will run their listeners in reverse order
 *
 * @see ListenerOrder
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReverseOrder {
}
