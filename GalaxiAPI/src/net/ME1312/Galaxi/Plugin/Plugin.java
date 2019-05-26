package net.ME1312.Galaxi.Plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Plugin Class Annotation<br>
 * Classes annotated with this will be loaded as plugins when found
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Plugin {
    /**
     * The Name of this Plugin
     *
     * @return Plugin Name
     */
    String name();

    /**
     * The Display Name of this Plugin
     *
     * @return Plugin Display Name
     */
    String display() default "";

    /**
     * The Version of this Plugin
     *
     * @return Plugin Version
     */
    String version();

    /**
     * The Build Signature of this Plugin
     *
     * @return Build Signature
     */
    String signature() default "";

    /**
     * The State of the Plugin
     *
     * @return Plugin State
     */
    String state() default "";

    /**
     * The Authors of this Plugin
     *
     * @return Authors List
     */
    String[] authors();

    /**
     * The Description of this Plugin
     *
     * @return Plugin Description
     */
    String description() default "";

    /**
     * The Authors' Website
     *
     * @return Authors' Website
     */
    String website() default "";

    /**
     * Load Before Plugins List
     *
     * @return Load Before List
     */
    String[] loadBefore() default {};

    /**
     * Dependencies List
     *
     * @return Dependencies List
     */
    Dependency[] dependencies() default {};
}