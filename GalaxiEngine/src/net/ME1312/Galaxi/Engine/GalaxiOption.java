package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Library.Container.Value;
import net.ME1312.Galaxi.Library.Try;

import java.awt.*;
import java.io.File;

import static net.ME1312.Galaxi.Engine.GalaxiOptionMode.*;

/**
 * Galaxi Option Enum
 *
 * @param <T> Option Type
 */
public final class GalaxiOption<T> extends Value<T> {

    // Directories
    public static final GalaxiOption<File> APPDATA_DIRECTORY = $(APP);
    public static final GalaxiOption<File> RUNTIME_DIRECTORY = $("user.dir", File::new, USR);
    public static final GalaxiOption<File> LOG_DIRECTORY = $(new File(RUNTIME_DIRECTORY.value(), "Logs"), APP_DEF);

    // Logging
    public static final GalaxiOption<Boolean> USE_LOG_FILE = $("galaxi.log", Boolean::parseBoolean, true, USR_APP_DEF);
    public static final GalaxiOption<Boolean> USE_RAW_LOG = $("galaxi.log.raw", Boolean::parseBoolean, GraphicsEnvironment.isHeadless(), USR_APP_DEF);
    public static final GalaxiOption<Boolean> USE_RAW_LOG_ANSI = $("galaxi.log.raw.ansi", Boolean::parseBoolean, USR_APP);
    public static final GalaxiOption<Boolean> SHOW_DEBUG_MESSAGES = $("galaxi.log.debug", Boolean::parseBoolean, false, USR_APP_DEF);
    public static final GalaxiOption<Boolean> COLOR_LOG_LEVELS = $("galaxi.log.color_levels", Boolean::parseBoolean, true, USR_APP_DEF);

    // Console
    public static final GalaxiOption<Boolean> SHOW_CONSOLE_WINDOW = $("galaxi.console", Boolean::parseBoolean, System.console() == null, USR_APP_DEF);
    public static final GalaxiOption<Boolean> PARSE_CONSOLE_VARIABLES = $("galaxi.console.parse_vars", Boolean::parseBoolean, false, USR_APP_DEF);
    public static final GalaxiOption<Integer> MAX_CONSOLE_WINDOW_SCROLLBACK = $("galaxi.console.max_scrollback", Integer::parseInt, 15000, USR_APP_DEF);
    public static final GalaxiOption<Double> CONSOLE_WINDOW_SIZE = $("galaxi.console.scaling", Double::parseDouble, USR);

    // Terminal
    public static final GalaxiOption<Boolean> USE_ANSI = $("galaxi.terminal.ansi", Boolean::parseBoolean, true, USR_DEF);
    public static final GalaxiOption<Boolean> USE_JLINE = $("galaxi.terminal.jline", Boolean::parseBoolean, true, USR_DEF);

    // Everything Else
    public static final GalaxiOption<Boolean> ENABLE_RELOAD = $(false, APP_DEF);

    private T app;
    private T value;
    private final T def;
    private final T usr;
    private final GalaxiOptionMode mode;
    private boolean uninitialized = true;
    private static boolean lock = false;
    GalaxiOption(T usr, T def, GalaxiOptionMode mode) {
        this.usr = usr;
        this.def = def;
        this.mode = mode;
    }

    /**
     * Sets the setting (as that which was requested by the app)
     *
     * @param value Value
     * @return Value
     */
    public T value(T value) {
        if (lock) throw new IllegalStateException("GalaxiOptions are locked in. Please adjust settings before launching GalaxiEngine.");
        return app = value;
    }

    /**
     * Grabs the default setting
     *
     * @return The default setting
     */
    public T def() {
        return def;
    }

    /**
     * Grabs the setting as set by the app
     *
     * @return The Apps Setting
     */
    public T app() {
        return app;
    }

    /**
     * Grabs the setting as set by the user
     *
     * @return The User's Setting
     */
    public T usr() {
        return usr;
    }

    /**
     * Grabs the setting as computed
     *
     * @return The Setting
     */
    @Override
    public T value() {
        if (uninitialized) {
            uninitialized = false;
            value = mode.select(usr, app, def);
        }
        return value;
    }


    private static <T> GalaxiOption<T> $(GalaxiOptionMode mode) {
        return $((T) null, mode);
    }
    private static <T> GalaxiOption<T> $(T def, GalaxiOptionMode mode) {
        return new GalaxiOption<T>(null, def, mode);
    }
    private static <T> GalaxiOption<T> $(Try.Supplier<T> def, GalaxiOptionMode mode) {
        return $(Try.all.get(def), mode);
    }
    private static <T> GalaxiOption<T> $(String usr, Try.Function<String, T> type, GalaxiOptionMode mode) {
        return $(usr, type, (T) null, mode);
    }
    private static <T> GalaxiOption<T> $(String usr, Try.Function<String, T> type, T def, GalaxiOptionMode mode) {
        String value = System.getProperty(usr);
        return new GalaxiOption<T>((value != null)?Try.all.get(() -> type.run(value)):null, def, mode);
    }
    private static <T> GalaxiOption<T> $(String usr, Try.Function<String, T> type, Try.Supplier<T> def, GalaxiOptionMode mode) {
        return $(usr, type, Try.all.get(def), mode);
    }
}
