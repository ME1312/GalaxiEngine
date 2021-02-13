package net.ME1312.Galaxi.Plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * App Class Annotation<br>
 * Classes annotated with this will <b>not</b> be loaded as plugins when found
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface App {
    /**
     * The Name of the App
     *
     * @return App Name
     */
    String name();

    /**
     * The Display Name of the App
     *
     * @return App Display Name
     */
    String display() default "";

    /**
     * The Version of this App
     *
     * @return App Version
     */
    String version();

    /**
     * The Build Version of the App
     *
     * @return App Build Version
     */
    String build() default "";

    /**
     * The State of the App
     *
     * @return App State
     */
    String state() default "";

    /**
     * The Authors of the App
     *
     * @return Authors List
     */
    String[] authors();

    /**
     * The Description of the App
     *
     * @return App Description
     */
    String description() default "";

    /**
     * The Authors' Website
     *
     * @return Authors' Website
     */
    String website() default "";
}