package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Util;

import java.awt.*;
import java.io.File;

/**
 * Galaxi Option Enum
 *
 * @param <T> Option Type
 */
public final class GalaxiOption<T> extends Container<T> {
    public static final GalaxiOption<File> APPLICATION_DIRECTORY = new GalaxiOption<>("user.dir", File::new);
    public static final GalaxiOption<Boolean> AUTO_SHOW_CONSOLE_WINDOW = new GalaxiOption<>("galaxi.ui.console", usr -> usr.length() == 0 || usr.equalsIgnoreCase("true"));
    public static final GalaxiOption<File> LOG_DIRECTORY = new GalaxiOption<>(null, usr -> new File(APPLICATION_DIRECTORY.get(), "Logs"));
    public static final GalaxiOption<Boolean> USE_RAW_LOG = new GalaxiOption<>("galaxi.log.raw", usr -> usr.equalsIgnoreCase("true") || (usr.length() == 0 && GraphicsEnvironment.isHeadless()));

    private static boolean lock = false;
    private final T def;
    private final String usr;
    private GalaxiOption(String usr, OptionConstructor<T> def) {
        super(Util.getDespiteException(() -> def.run((usr == null)?"":System.getProperty(usr, "")), null));
        this.usr = (usr == null)?null:System.getProperty(usr, "");
        this.def = get();
    }
    private interface OptionConstructor<T> {
        T run(String usr) throws Throwable;
    }

    @Override
    public void set(T value) {
        if (lock) throw new IllegalStateException("GalaxiOptions have been locked in. Please adjust settings before launching GalaxiEngine");
        super.set(value);
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
     * Grabs the Object Value set by the user
     *
     * @return The User's Object Value
     */
    public String usr() {
        return usr;
    }
}
