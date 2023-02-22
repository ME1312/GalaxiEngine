package net.ME1312.Galaxi.Event;

import java.lang.annotation.*;

/**
 * Reverse Order Event Annotation<br>
 * Events annotated by this will run their listeners in reverse order
 *
 * @see ListenerOrder
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ReverseOrder {

    /**
     * This can be set to revert the effect of this annotation
     *
     * @return Reversed Status
     */
    boolean value() default true;
}
