package net.ME1312.Galaxi.Log;

import net.ME1312.Galaxi.Library.Util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.PrimitiveIterator;
import java.util.concurrent.ExecutorService;

import static net.ME1312.Galaxi.Log.LogLevel.*;

/**
 * Logger Class
 */
public final class Logger {
    private static boolean color = false;
    private static ExecutorService service = null;
    private static LogStream.MessageHandler writer = null;

    private static final LinkedList<LogFilter> gFilters = new LinkedList<LogFilter>();
    private final LinkedList<LogFilter> lFilters = new LinkedList<LogFilter>();
    private final java.util.logging.Logger primitive;
    final String prefix;

    /**
     * Gets a new Logger
     *
     * @param prefix Log Prefix
     */
    public Logger(String prefix) {
        if (Util.isNull(prefix)) throw new NullPointerException();
        if (prefix.length() == 0) throw new StringIndexOutOfBoundsException("Cannot use an empty prefix");
        debug = new LogStream(this, DEBUG);
        message = new LogStream(this, MESSAGE);
        info = new LogStream(this, INFO);
        success = new LogStream(this, SUCCESS);
        warn = new LogStream(this, WARN);
        error = new LogStream(this, ERROR);
        severe = new LogStream(this, SEVERE);

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

    private static LogStream last = null;
    private static boolean terminated = true;
    static void log(LogStream stream, Date time, String original) {
        if (service != null) service.submit(() -> {
            try {
                String prefix = '[' + new SimpleDateFormat("HH:mm:ss").format(time) + "] [" + stream.getLogger().getPrefix() + File.separator + stream.getLevel().getName() + "] > ";
                StringBuilder result = new StringBuilder();

                boolean terminate = false;
                LinkedList<String> messages = new LinkedList<String>();
                if (original.length() > 0) {
                    StringBuilder message = new StringBuilder();
                    boolean terminate_with_prefix = false;
                    for (PrimitiveIterator.OfInt $i = original.codePoints().iterator(); $i.hasNext();) {
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
                            case '\t':
                                message.append("   ");
                                c = ' ';
                            default:
                                message.appendCodePoint(c);
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
                            if (color && stream.getLevel().getColor() != null) result.append(stream.getLevel().getColor());
                            result.append(prefix);
                            if (color && stream.getLevel().getColor() != null) result.append("\u001B[m");
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

                if (result.length() > 0) writer.log(result.toString());
            } catch (Throwable e) {
                // No way to log, I suppose.
            }
        });
    }
}
