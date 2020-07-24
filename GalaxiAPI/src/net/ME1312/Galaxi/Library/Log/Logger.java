package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Library.Container.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import org.fusesource.jansi.Ansi;

import java.awt.*;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.PrimitiveIterator;
import java.util.concurrent.TimeUnit;

import static net.ME1312.Galaxi.Library.Log.LogLevel.*;

/**
 * Logger Class
 */
public final class Logger {
    private static final Container<StringOutputStream> pso = new Container<StringOutputStream>(null);
    private static boolean running = true;
    static final LinkedList<NamedContainer<LogStream, String>> messages = new LinkedList<NamedContainer<LogStream, String>>();
    private static final LinkedList<LogFilter> gFilters = new LinkedList<LogFilter>();
    private LinkedList<LogFilter> lFilters = new LinkedList<LogFilter>();
    private final java.util.logging.Logger primitive;
    private static Thread thread;
    final String prefix;

    /**
     * Gets a new Logger
     *
     * @param prefix Log Prefix
     */
    public Logger(String prefix) {
        if (Util.isNull(prefix)) throw new NullPointerException();
        if (prefix.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty prefix");
        debug = new LogStream(this, DEBUG, pso);
        message = new LogStream(this, MESSAGE, pso);
        info = new LogStream(this, INFO, pso);
        success = new LogStream(this, SUCCESS, pso);
        warn = new LogStream(this, WARN, pso);
        error = new LogStream(this, ERROR, pso);
        severe = new LogStream(this, SEVERE, pso);

        primitive = java.util.logging.Logger.getAnonymousLogger();
        primitive.setUseParentHandlers(false);
        primitive.addHandler(new LogTranslator(this));

        this.prefix = prefix;
    }

    public final LogStream debug;
    public final LogStream message;
    public final LogStream info;
    public final LogStream success;
    public final LogStream warn;
    public final LogStream error;
    public final LogStream severe;

    /**
     * Get the stream by Log Level
     *
     * @param level Log Level
     * @return Log Stream
     */
    public LogStream get(LogLevel level) {
        switch (level) {
            case DEBUG:
                return debug;
            case MESSAGE:
                return message;
            case INFO:
                return info;
            case SUCCESS:
                return success;
            case WARN:
                return warn;
            case ERROR:
                return error;
            case SEVERE:
                return severe;
            default:
                return null;
        }
    }

    /**
     * Get this logger as a standard Java Logger
     *
     * @return Standard Java Logger
     */
    public java.util.logging.Logger toPrimitive() {
        return primitive;
    }

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

    private static void log(boolean COLOR_LEVELS) {
        long refresh = TimeUnit.SECONDS.toNanos(1) / Math.max(Util.getDespiteException(() -> GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getRefreshRate(), 30), 30);
        long refreshMillis = (long) Math.floor(refresh / 1000000d);
        int refreshNanos = (int) (refresh - (refreshMillis * 1000000));

        (thread = new Thread(() -> {
            LogStream last = null;
            boolean terminated = true;
            while (running) {
                while (running && messages.size() > 0) try {
                    NamedContainer<LogStream, String> container = Util.getDespiteException(() -> messages.get(0), null);
                    if (container != null) {
                        LogStream stream = container.name();
                        String prefix = '[' + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "] [" + stream.getLogger().getPrefix() + File.separator + stream.getLevel().getName() + "] > ";
                        StringBuilder result = new StringBuilder();

                        LinkedList<String> messages = new LinkedList<String>();
                        boolean terminate = false;
                        if (container.get().length() > 0) {
                            StringBuilder message = new StringBuilder();
                            boolean terminate_with_prefix = false;
                            for (PrimitiveIterator.OfInt $i = container.get().codePoints().iterator(); $i.hasNext();) {
                                int c = $i.nextInt();
                                if (terminate) {
                                    messages.add(message.toString());
                                    message = new StringBuilder();
                                }

                                switch (c) {
                                    case '\n':
                                        terminate = true;
                                        break;
                                    case '\r':
                                        terminate_with_prefix = true;
                                        break;
                                    default:
                                        switch (c) {
                                            case '\t':
                                                message.append("    ");
                                                break;
                                            default:
                                                message.appendCodePoint(c);
                                                break;
                                        }
                                        terminate = false;
                                        terminate_with_prefix = false;
                                        break;
                                }
                            }
                            if (message.length() > 0) messages.add(message.toString());
                            if (terminate && (terminate_with_prefix || messages.size() <= 0)) messages.add("");
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
                                    if (!terminated) result.append('\n');
                                    if (COLOR_LEVELS && stream.getLevel().getColor() != null) result.append(stream.getLevel().getColor().toString());
                                    result.append(prefix);
                                    if (COLOR_LEVELS && stream.getLevel().getColor() != null) result.append(Ansi.ansi().reset().toString());
                                }
                                last = stream;
                                logged = true;
                                terminated = false;
                                result.append(message);

                                if (i < messages.size()) {
                                    terminated = true;
                                    result.append('\n');
                                }
                            }
                        }
                        if (logged && terminate) {
                            terminated = true;
                            result.append('\n');
                        }

                        if (result.length() > 0)
                            stream.stream.get().write(result.toString());
                    }
                    Util.isException(() -> Logger.messages.remove(0));
                } catch (Throwable e) {
                    Util.isException(() -> Logger.messages.remove(0));
                    //if (pso.get() != null) e.printStackTrace(pso.get());
                }
                Util.isException(() -> Thread.sleep(refreshMillis, refreshNanos));
            }
        }, Galaxi.getInstance().getEngineInfo().getName() + "::Log_Spooler")).start();
    }
}
