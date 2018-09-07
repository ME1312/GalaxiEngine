package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Util;

import java.io.PrintStream;

/**
 * Logger Class
 */
public final class Logger {
    private static Container<PrintStream> pso = new Container<PrintStream>(null);
    private static Container<PrintStream> pse = new Container<PrintStream>(null);

    /**
     * Gets a new Logger
     *
     * @param prefix Log Prefix
     */
    public Logger(String prefix) {
        if (Util.isNull(prefix)) throw new NullPointerException();
        if (prefix.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty prefix");
        message = new LogStream(prefix, "MESSAGE", pso);
        info = new LogStream(prefix, "INFO", pso);
        warn = new ErrorStream(prefix, "WARN", pso);
        error = new ErrorStream(prefix, "ERROR", pse);
        severe = new ErrorStream(prefix, "SEVERE", pse);
    }

    public final LogStream message;
    public final LogStream info;
    public final ErrorStream warn;
    public final ErrorStream error;
    public final ErrorStream severe;
}
