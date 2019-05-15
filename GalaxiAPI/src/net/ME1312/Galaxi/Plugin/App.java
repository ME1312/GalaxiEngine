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
     * The Display Name of the APp
     *
     * @return App Display Name
     */
    String display() default "";

    /**
     * The Version of this Plugin
     *
     * @return App Version
     */
    String version();

    /**
     * The Build Signature of the App
     *
     * @return Build Signature
     */
    String signature() default "";

    /**
     * The Authors of the App
     *
     * @return Authors List
     */
    String[] authors();

    /**
     * The Description of the App
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
}