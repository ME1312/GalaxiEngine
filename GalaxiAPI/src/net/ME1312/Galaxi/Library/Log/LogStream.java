package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Container;

import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Log Stream Class
 */
public final class LogStream {
    private static LogStream last = null;
    private Logger logger;
    private String name;
    private Container<PrintStream> stream;
    private boolean first = true;
    private Thread threadwriting = null;
    protected long writing = 0;

    LogStream(Logger logger, String name, Container<PrintStream> stream) {
        this.logger = logger;
        this.name = name;
        this.stream = stream;
    }

    String prefix() {
        return "[" + new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) + "] [" + logger.prefix + File.separator + name + "] > ";
    }

    /**
     * Print an Object
     *
     * @param obj Object
     */
    public void print(Object obj) {
        sync();
        writing++;
        if (obj == null) {
            for (char c : "null".toCharArray()) write(c);
        } else {
            for (char c : obj.toString().toCharArray()) write(c);
        }
        writing--;
    }

    /**
     * Print an Exception
     *
     * @param err Exception
     */
    public void print(Throwable err) {
        sync();
        writing++;
        StringWriter sw = new StringWriter();
        err.printStackTrace(new PrintWriter(sw));
        String s = sw.toString();
        for (char c : s.substring(0, s.length() - 1).toCharArray()) write(c);
        writing--;
    }

    /**
     * Print a String
     *
     * @param str String
     */
    public void print(String str) {
        sync();
        writing++;
        if (str == null) {
            for (char c : "null".toCharArray()) write(c);
        } else {
            for (char c : str.toCharArray()) write(c);
        }
        writing--;
    }

    /**
     * Print an Array of Characters
     *
     * @param str Character Array
     */
    public void print(char[] str) {
        sync();
        writing++;
        for (char c : str) write(c);
        writing--;
    }

    /**
     * Print a Character
     *
     * @param c Character
     */
    public void print(char c) {
        sync();
        writing++;
        write(c);
        writing--;
    }

    /**
     * Write to the PrintStream
     *
     * @param c Character
     */
    protected void write(char c) {
        threadwriting = Thread.currentThread();
        if (last != this) {
            if (last != null) {
                stall();
                if (!last.first) last.print('\n');
            }
            LogStream.last = this;
            first = true;
        }
        if (first) stream.get().print(prefix());
        stream.get().print(c);
        first = c == '\n';
    }

    private void stall() {
        try {
            while (last != null && last != this && last.writing > 0) {
                Thread.sleep(125);
            }
        } catch (Exception e) {}
    }

    protected void sync() {
        try {
            while (threadwriting != null && threadwriting != Thread.currentThread() && writing > 0) {
                Thread.sleep(125);
            }
        } catch (Exception e) {}
    }

    /**
     * Print an empty line
     */
    public void println() {
        sync();
        writing++;
        write('\n');
        writing--;
    }

    /**
     * Print multiple Objects (separated by a new line)
     *
     * @param obj Objects
     */
    public void println(Object... obj) {
        sync();
        writing++;
        for (Object OBJ : obj) {
            if (OBJ == null) {
                for (char c : "null".toCharArray()) write(c);
            } else {
                for (char c : OBJ.toString().toCharArray()) write(c);
            }
            write('\n');
        }
        writing--;
    }

    /**
     * Print multiple Exceptions (separated by a new line)
     *
     * @param err Exceptions
     */
    public void println(Throwable... err) {
        sync();
        writing++;
        for (Throwable e : err) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            for (char c : sw.toString().toCharArray()) write(c);
        }
        writing--;
    }

    /**
     * Print multiple Strings (separated by a new line)
     *
     * @param str Objects
     */
    public void println(String... str) {
        sync();
        writing++;
        for (String STR : str) {
            if (STR == null) {
                for (char c : "null".toCharArray()) write(c);
            } else {
                for (char c : STR.toCharArray()) write(c);
            }
            write('\n');
        }
        writing--;
    }

    /**
     * Print multiple Arrays of Characters (separated by a new line)
     *
     * @param str Character Arrays
     */
    public void println(char[]... str) {
        sync();
        writing++;
        for (char[] STR : str) {
            for (char c : STR) write(c);
            write('\n');
        }
        writing--;
    }

    /**
     * Print multiple Characters (separated by a new line)
     *
     * @param c Characters
     */
    public void println(char... c) {
        sync();
        writing++;
        for (char C : c) {
            write(C);
            write('\n');
        }
        writing--;
    }
}
