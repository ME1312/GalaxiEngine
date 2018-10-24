package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

import static net.ME1312.Galaxi.Library.Log.LogLevel.*;

/**
 * Logger Class
 */
public final class Logger {
    private static final Container<PrintStream> pso = new Container<PrintStream>(null);
    private static final Container<PrintStream> pse = new Container<PrintStream>(null);
    private static final boolean running = true;
    static final LinkedList<NamedContainer<LogStream, String>> messages = new LinkedList<NamedContainer<LogStream, String>>();
    private static final LinkedList<LogFilter> gFilters = new LinkedList<LogFilter>();
    private static Thread thread;
    private LinkedList<LogFilter> lFilters = new LinkedList<LogFilter>();
    final String prefix;

    /**
     * Gets a new Logger
     *
     * @param prefix Log Prefix
     */
    public Logger(String prefix) {
        if (Util.isNull(prefix)) throw new NullPointerException();
        if (prefix.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty prefix");
        message = new LogStream(this, MESSAGE, pso);
        info = new LogStream(this, INFO, pso);
        warn = new LogStream(this, WARN, pso);
        error = new LogStream(this, ERROR, pse);
        severe = new LogStream(this, SEVERE, pse);

        this.prefix = prefix;
        if (thread == null || !thread.isAlive()) log();
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

    /**
     * Add a filter to this logger
     *
     * @param filter Log Filter
     */
    public void addFilter(LogFilter filter) {
        if (Util.isNull(filter)) throw new NullPointerException();
        lFilters.add(filter);
    }

    /**
     * Remove a filter from this logger
     *
     * @param filter Log Filter
     */
    public void removeFilter(LogFilter filter) {
        lFilters.remove(filter);

    }

    /**
     * Add a filter to all loggers
     *
     * @param filter Static Log Filter
     */
    public static void addStaticFilter(LogFilter filter) {
        if (Util.isNull(filter)) throw new NullPointerException();
        gFilters.add(filter);
    }

    /**
     * Remove a static filter
     *
     * @param filter Static Log Filter
     */
    public static void removeStaticFilter(LogFilter filter) {
        gFilters.remove(filter);
    }

    private static void log() {
        (thread = new Thread(() -> {
            LogStream last = null;
            boolean terminated = true;
            while (running) {
                while (messages.size() > 0) {
                    NamedContainer<LogStream, String> container = Util.getDespiteException(() -> messages.get(0), null);
                    if (container != null) {
                        LogStream stream = container.name();
                        String prefix = '[' + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "] [" + stream.getLogger().getPrefix() + File.separator + stream.getLevel() + "] > ";
                        LinkedList<String> messages = new LinkedList<String>();
                        boolean terminate = false;
                        if (container.get().length() > 0) {
                            String message = "";
                            boolean terminate_with_prefix = false;
                            for (char c : container.get().toCharArray()) {
                                if (terminate) {
                                    messages.add(message);
                                    message = "";
                                }

                                switch (c) {
                                    case '\n':
                                        terminate = true;
                                        break;
                                    case '\r':
                                        terminate_with_prefix = true;
                                        break;
                                    default:
                                        if (!terminate && terminate_with_prefix) message += '\r';
                                        message += c;
                                        terminate = false;
                                        terminate_with_prefix = false;
                                        break;
                                }
                            }
                            if (!terminate && terminate_with_prefix) message += '\r';
                            if (message.length() > 0) messages.add(message);
                            if (terminate && (terminate_with_prefix || terminated)) messages.add("");
                        }

                        int i = 0;
                        boolean logged = messages.size() <= 0;
                        for (String message : messages) {
                            i++;

                            Boolean response = null;
                            LinkedList<LogFilter> cache = new LinkedList<LogFilter>();
                            cache.addAll(stream.getLogger().lFilters);
                            cache.addAll(gFilters);
                            for (LogFilter filter : cache) try {
                                response = filter.filter(stream, message);
                                if (response != null) break;
                            } catch (Throwable e) {}

                            if (response == null || response == Boolean.TRUE) {
                                if (terminated || last != stream) {
                                    if (!terminated) stream.stream.get().print('\n');
                                    stream.stream.get().print(prefix);
                                }
                                last = stream;
                                logged = true;
                                terminated = false;
                                stream.stream.get().print('\u0000' + message + '\u0000');

                                if (i < messages.size()) {
                                    terminated = true;
                                    stream.stream.get().print('\n');
                                }
                            }
                        }
                        if (logged && terminate) {
                            terminated = true;
                            stream.stream.get().print('\n');
                        }

                        Logger.messages.remove(0);
                    }
                }
                Util.isException(() -> Thread.sleep(32));
            }
        })).start();
    }
}
