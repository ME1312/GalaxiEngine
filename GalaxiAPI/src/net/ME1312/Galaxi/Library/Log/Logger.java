package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Util;

import java.io.PrintStream;

/**
 * Logger Class
 */
public final class Logger {
    private static final Container<PrintStream> pso = new Container<PrintStream>(null);
    private static final Container<PrintStream> pse = new Container<PrintStream>(null);
    final String prefix;

    /**
     * Gets a new Logger
     *
     * @param prefix Log Prefix
     */
    public Logger(String prefix) {
        if (Util.isNull(prefix)) throw new NullPointerException();
        if (prefix.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty prefix");
        message = new LogStream(this, "MESSAGE", pso);
        info = new LogStream(this, "INFO", pso);
        warn = new LogStream(this, "WARN", pso);
        error = new LogStream(this, "ERROR", pse);
        severe = new LogStream(this, "SEVERE", pse);

        this.prefix = prefix;
    }

    public final LogStream message;
    public final LogStream info;
    public final LogStream warn;
    public final LogStream error;
    public final LogStream severe;

    /**
     * Get the prefix this logger uses
     *
     * @return Logger Prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
