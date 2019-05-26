package net.ME1312.Galaxi.Plugin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Plugin Dependency Annotation
 *
 * @see Plugin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Dependency {
    /**
     * Name of the Dependency
     *
     * @return Dependency Name
     */
    String name();

    /**
     * Minimum required version of the Dependency
     *
     * @return Minimum required version
     */
    String minVersion() default "";

    /**
     * Maximum allowed version of the Dependency
     *
     * @return Maximum allowed version
     */
    String maxVersion() default "";

    /**
     * Whether the Dependency is required
     *
     * @return Required Dependency Status
     */
    boolean required() default true;
}
