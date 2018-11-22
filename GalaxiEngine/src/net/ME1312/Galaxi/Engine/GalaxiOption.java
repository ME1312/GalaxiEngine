package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Util;

import java.io.File;

public final class GalaxiOption<T> extends Container<T> {
    public static final GalaxiOption<File> APPLICATION_DIRECTORY = new GalaxiOption<>(new File(System.getProperty("user.dir")));
    public static final GalaxiOption<Boolean> AUTO_SHOW_CONSOLE_WINDOW = new GalaxiOption<>(System.getProperty("galaxi.ui.console", "true").equalsIgnoreCase("true"));
    public static final GalaxiOption<File> LOG_DIRECTORY = new GalaxiOption<>(new File(APPLICATION_DIRECTORY.get(), "Logs"));

    private GalaxiOption(T def) {
        super(def);
    }
    private GalaxiOption(Util.ReturnRunnable<T> def) {
        this(def.run());
    }
    private GalaxiOption(Util.ExceptionReturnRunnable<T> def, T fallback) {
        this(Util.getDespiteException(def, fallback));
    }
}
