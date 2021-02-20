package net.ME1312.Galaxi.Log;

import net.ME1312.Galaxi.Library.Util;

import java.io.*;
import java.util.Calendar;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Log Stream Class
 */
public final class LogStream {
    private final Logger logger;
    private final LogLevel level;
    private final PrintStream primitive;

    LogStream(Logger logger, LogLevel level) {
        this.logger = logger;
        this.level = level;
        this.primitive = Util.getDespiteException(() -> new PrintStream(new OutputStream() {
            final ByteArrayOutputStream pending = new ByteArrayOutputStream();

            @Override
            public void write(int b) {
                pending.write(b);
            }

            @Override
            public void flush() throws IOException {
                if (pending.size() > 0) {
                    print(pending.toString(UTF_8.name()).replace("\r", ""));
                    pending.reset();
                }
            }
        }, true, UTF_8.name()), null);
    }

    interface MessageHandler {
        void log(String message) throws IOException;
    }

    /**
     * Get this logger as a standard Java PrintStream
     *
     * @return Standard Java PrintStream
     */
    public PrintStream toPrimitive() {
        return primitive;
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
     * Get the level this stream logs on
     *
     * @return Log Level
     */
    public LogLevel getLevel() {
        return level;
    }

    private void submit(String str) {
        Logger.log(this, Calendar.getInstance().getTime(), str);
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private String convert(TextElement original) {
        StringBuilder message = new StringBuilder();
        if (original != null) {
            try {
                for (TextElement e : original.before) message.append(convert(e));
            } catch (Throwable e) {
                getLogger().error.println(e);
            }

            ConsoleTextElement element = new ConsoleTextElement(original.element);
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
                for (TextElement e : original.after) message.append(convert(e));
            } catch (Throwable e) {
                getLogger().error.println(e);
            }
        } else {
            message.append("null");
        }

        // hack for formatting over newlines
        int length = message.codePointCount(0, message.length());
        if (length > 3 && message.codePointAt(length - 4) == '\n') {
            return message.substring(0, message.length() - 3);
        } else return message.toString();
    }

    /**
     * Print an Object
     *
     * @param obj Object
     */
    public void print(Object obj) {
        if (obj == null) {
            submit("null");
        } else if (obj instanceof Throwable) {
            print((Throwable) obj);
        } else if (obj instanceof TextElement) {
            print((TextElement) obj);
        } else {
            submit(obj.toString());
        }
    }

    /**
     * Print an Exception
     *
     * @param err Exception
     */
    public void print(Throwable err) {
        if (err == null) {
            submit("null");
        } else {
            StringWriter sw = new StringWriter();
            err.printStackTrace(new PrintWriter(sw));
            submit(sw.toString());
        }
    }

    /**
     * Print a Text Element
     *
     * @param element Text Element
     */
    public void print(TextElement element) {
        submit(convert(element));
    }

    /**
     * Print a String
     *
     * @param str String
     */
    public void print(String str) {
        if (str == null) {
            submit("null");
        } else {
            submit(str);
        }
    }

    /**
     * Print an Array of Characters
     *
     * @param str Character Array
     */
    public void print(char[] str) {
        if (str == null) {
            submit("null");
        } else {
            submit(new String(str));
        }
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
        submit("\r\n");
    }

    /**
     * Print multiple Objects (separated by a new line)
     *
     * @param obj Objects
     */
    public void println(Object... obj) {
        for (Object OBJ : obj) {
            if (OBJ == null) {
                submit("null\n");
            } else if (OBJ instanceof Throwable) {
                println((Throwable) OBJ);
            } else if (OBJ instanceof TextElement) {
                println((TextElement) OBJ);
            } else {
                submit(OBJ.toString() + '\n');
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
                submit("null\n");
            } else {
                StringWriter sw = new StringWriter();
                ERR.printStackTrace(new PrintWriter(sw));
                submit(sw.toString() + '\n');
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
            submit(convert(ELEMENT) + '\n');
        }
    }

    /**
     * Print multiple Strings (separated by a new line)
     *
     * @param str Objects
     */
    public void println(String... str) {
        for (String STR : str) {
            if (STR == null) {
                submit("null\n");
            } else {
                submit(STR + '\n');
            }
        }
    }

    /**
     * Print multiple Arrays of Characters (separated by a new line)
     *
     * @param str Character Arrays
     */
    public void println(char[]... str) {
        for (char[] STR : str) {
            if (STR == null) {
                submit("null\n");
            } else {
                submit(new String(STR) + '\n');
            }
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
