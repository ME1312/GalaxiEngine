package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Library.Callback.ExceptionReturnCallback;
import net.ME1312.Galaxi.Library.Callback.ExceptionReturnRunnable;
import net.ME1312.Galaxi.Library.Container.Value;
import net.ME1312.Galaxi.Library.Platform;
import net.ME1312.Galaxi.Library.Util;

import java.awt.*;
import java.io.File;

/**
 * Galaxi Option Enum
 *
 * @param <T> Option Type
 */
public final class GalaxiOption<T> extends Value<T> {

    // Directories
    public static final GalaxiOption<File> APPDATA_DIRECTORY = new GalaxiOption<>(() -> Platform.getSystem().getAppDataDirectory());
    public static final GalaxiOption<File> RUNTIME_DIRECTORY = new GalaxiOption<>("user.dir", File::new);
    public static final GalaxiOption<File> LOG_DIRECTORY = new GalaxiOption<>(() -> new File(RUNTIME_DIRECTORY.value(), "Logs"));

    // Logging
    public static final GalaxiOption<Boolean> USE_LOG_FILE = new GalaxiOption<>("galaxi.log", usr -> usr.length() == 0 || usr.equalsIgnoreCase("true"));
    public static final GalaxiOption<Boolean> USE_RAW_LOG = new GalaxiOption<>("galaxi.log.raw", usr -> usr.equalsIgnoreCase("true") || (usr.length() == 0 && GraphicsEnvironment.isHeadless()));
    public static final GalaxiOption<Boolean> SHOW_DEBUG_MESSAGES = new GalaxiOption<>("galaxi.log.debug", usr -> usr.equalsIgnoreCase("true"));
    public static final GalaxiOption<Boolean> COLOR_LOG_LEVELS = new GalaxiOption<>("galaxi.log.color_levels", usr -> usr.length() == 0 || usr.equalsIgnoreCase("true"));

    // Console
    public static final GalaxiOption<Boolean> SHOW_CONSOLE_WINDOW = new GalaxiOption<>("galaxi.console", usr -> usr.length() == 0 || usr.equalsIgnoreCase("true"));
    public static final GalaxiOption<Boolean> PARSE_CONSOLE_VARIABLES = new GalaxiOption<>("galaxi.console.parse_vars", usr -> usr.equalsIgnoreCase("true"));
    public static final GalaxiOption<Integer> MAX_CONSOLE_WINDOW_SCROLLBACK = new GalaxiOption<Integer>("galaxi.console.max_scrollback", usr -> (Util.getDespiteException(() -> Integer.parseInt(usr), 0) > 0)?Integer.parseInt(usr):15000);
    public static final GalaxiOption<Double> CONSOLE_WINDOW_SIZE = new GalaxiOption<>("galaxi.console.scaling", usr -> (Util.getDespiteException(() -> Double.parseDouble(usr), 0D) > 0)?Double.parseDouble(usr):1);

    // Terminal
    public static final GalaxiOption<Boolean> USE_ANSI = new GalaxiOption<>("galaxi.terminal.ansi", usr -> usr.length() == 0 || usr.equalsIgnoreCase("true"));
    public static final GalaxiOption<Boolean> USE_JLINE = new GalaxiOption<>("galaxi.terminal.jline", usr -> usr.length() == 0 || usr.equalsIgnoreCase("true"));

    // Everything Else
    public static final GalaxiOption<Boolean> ENABLE_RELOAD = new GalaxiOption<>(() -> false);


    private T app;
    private final T def;
    private final String usr;
    private static boolean lock = false;
    private GalaxiOption(ExceptionReturnRunnable<T> def) {
        this(null, usr -> def.run());
    }
    private GalaxiOption(String usr, ExceptionReturnCallback<String, T> def) {
        this.app = Util.getDespiteException(() -> def.run((usr == null)?"":System.getProperty(usr, "")), null);
        this.usr = (usr == null)?null:System.getProperty(usr, "");
        this.def = value();
    }

    @Override
    public T value(T value) {
        if (lock) throw new IllegalStateException("GalaxiOptions have been locked in. Please adjust settings before launching GalaxiEngine");
        return app = value;
    }

    /**
     * Grabs the Object Value as set by the app
     *
     * @return The Apps Object Value
     */
    @Override
    public T value() {
        return app;
    }

    /**
     * Grabs the Default Object Value
     *
     * @return The Default Object Value
     */
    public T def() {
        return def;
    }

    /**
     * Grabs the Object Value as set by the app
     *
     * @return The Apps Object Value
     */
    public T app() {
        return value();
    }

    /**
     * Grabs the Object Value as set by the user
     *
     * @return The User's Object Value
     */
    public String usr() {
        return usr;
    }
}
