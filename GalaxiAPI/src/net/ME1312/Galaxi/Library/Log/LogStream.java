package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.TextElement;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.LinkedList;

/**
 * Log Stream Class
 */
public final class LogStream {
    private Logger logger;
    private LogLevel level;
    Container<PrintStream> stream;

    LogStream(Logger logger, LogLevel level, Container<PrintStream> stream) {
        this.logger = logger;
        this.level = level;
        this.stream = stream;
    }

    /**
     * Get the Logger this stream belongs to
     *
     * @return Logger
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Get the prefix this logger uses
     *
     * @return Logger Prefix
     */
    public String getPrefix() {
        return logger.prefix;
    }

    /**
     * Ge the level this stream logs on
     *
     * @return Log Level
     */
    public LogLevel getLevel() {
        return level;
    }

    /**
     * Write to the PrintStream
     *
     * @param str String
     */
    private void write(String str) {
        Logger.messages.add(new NamedContainer<LogStream, String>(this, str));
    }

    @SuppressWarnings({"unchecked", "StringConcatenationInsideStringBufferAppend"})
    private String convert(TextElement original) {
        StringBuilder message = new StringBuilder();
        ConsoleTextElement element = new ConsoleTextElement(original.toRaw());
        try {
            Field f = TextElement.class.getDeclaredField("before");
            f.setAccessible(true);
            for (TextElement e : (LinkedList<TextElement>) f.get(element)) message.append(convert(e));
            f.setAccessible(false);
        } catch (Throwable e) {
            getLogger().error.println(e);
        }

        if (element.bold()) message.append("\u001B[1m");
        if (element.italic()) message.append("\u001B[3m");
        if (element.underline()) message.append("\u001B[4m");
        if (element.strikethrough()) message.append("\u001B[9m");
        if (element.color() != null) {
            int red = element.color().getRed();
            int green = element.color().getGreen();
            int blue = element.color().getBlue();
            float alpha = element.color().getAlpha() / 255f;

            red = Math.round(alpha * red);
            green = Math.round(alpha * green);
            blue = Math.round(alpha * blue);

            message.append("\u001B[38;2;" + red + ";" + green + ";" + blue + "m");
        }
        if (element.backgroundColor() != null) {
            int red = element.backgroundColor().getRed();
            int green = element.backgroundColor().getGreen();
            int blue = element.backgroundColor().getBlue();
            float alpha = element.backgroundColor().getAlpha() / 255f;

            red = Math.round(alpha * red);
            green = Math.round(alpha * green);
            blue = Math.round(alpha * blue);

            message.append("\u001B[48;2;" + red + ";" + green + ";" + blue + "m");
        }
        if (element.onClick() != null) message.append("\033]99900;" + element.onClick().toString() + "\007");
        message.append(element.message());
        message.append("\u001B[m");

        try {
            Field f = TextElement.class.getDeclaredField("after");
            f.setAccessible(true);
            for (TextElement e : (LinkedList<TextElement>) f.get(element)) message.append(convert(e));
            f.setAccessible(false);
        } catch (Throwable e) {
            getLogger().error.println(e);
        }

        return message.toString();
    }

    /**
     * Print an Object
     *
     * @param obj Object
     */
    public void print(Object obj) {
        if (obj == null) {
            print("null");
        } else {
            print(obj.toString());
        }
    }

    /**
     * Print an Exception
     *
     * @param err Exception
     */
    public void print(Throwable err) {
        if (err == null) {
            print("null");
        } else {
            StringWriter sw = new StringWriter();
            err.printStackTrace(new PrintWriter(sw));
            print(sw.toString());
        }
    }

    /**
     * Print a Text Element
     *
     * @param element Text Element
     */
    public void print(TextElement element) {
        if (element == null) {
            write("null");
        } else {
            write(convert(element));
        }
    }

    /**
     * Print a String
     *
     * @param str String
     */
    public void print(String str) {
        if (str == null) {
            write("null");
        } else {
            write(str);
        }
    }

    /**
     * Print an Array of Characters
     *
     * @param str Character Array
     */
    public void print(char[] str) {
        print(new String(str));
    }

    /**
     * Print a Character
     *
     * @param c Character
     */
    public void print(char c) {
        print(new char[]{c});
    }

    /**
     * Print an empty line
     */
    public void println() {
        print("\r\n");
    }

    /**
     * Print multiple Objects (separated by a new line)
     *
     * @param obj Objects
     */
    public void println(Object... obj) {
        for (Object OBJ : obj) {
            if (OBJ == null) {
                print("null\n");
            } else {
                print(OBJ.toString() + '\n');
            }
        }
    }

    /**
     * Print multiple Exceptions (separated by a new line)
     *
     * @param err Exceptions
     */
    public void println(Throwable... err) {
        for (Throwable ERR : err) {
            if (ERR == null) {
                print("null\n");
            } else {
                StringWriter sw = new StringWriter();
                ERR.printStackTrace(new PrintWriter(sw));
                print(sw.toString() + '\n');
            }
        }
    }

    /**
     * Print a Text Element
     *
     * @param element Text Element
     */
    public void println(TextElement... element) {
        for (TextElement ELEMENT : element) {
            write(convert(ELEMENT) + '\n');
        }
    }

    /**
     * Print multiple Strings (separated by a new line)
     *
     * @param str Objects
     */
    public void println(String... str) {
        for (String STR : str) {
            print(STR + '\n');
        }
    }

    /**
     * Print multiple Arrays of Characters (separated by a new line)
     *
     * @param str Character Arrays
     */
    public void println(char[]... str) {
        for (char[] STR : str) {
            print(new String(STR) + '\n');
        }
    }

    /**
     * Print multiple Characters (separated by a new line)
     *
     * @param c Characters
     */
    public void println(char... c) {
        for (char C : c) {
            print(new char[]{C, '\n'});
        }
    }
}
