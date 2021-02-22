package net.ME1312.Galaxi.Log;

import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;

import java.awt.*;
import java.io.*;
import java.util.*;

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
        Color parse256(int color);
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

    private String convert(TextElement original) {
        return convert(original, null);
    }

    private enum StyleElement {
        BOLD("22"),
        ITALIC("23"),
        UNDERLINE("24"),
        STRIKETHROUGH("29"),
        FG_COLOR("39"),
        BG_COLOR("49"),
        SCRIPT_SHIFT("75"),
        HYPERLINK(null);

        private final String reset;
        StyleElement(String reset) {
            this.reset = reset;
        }
    }

    private String convert(TextElement original, LinkedHashMap<StyleElement, String> master) {
        StringBuilder message = new StringBuilder();
        if (original != null) {
            Map<StyleElement, String> existing = (master == null)? Collections.emptyMap() : master;

            // Write Before Queue
            try {
                for (TextElement e : original.before) message.append(convert(e));
            } catch (Throwable e) {
                getLogger().error.println(e);
            }

            // Calculate Style Elements
            ConsoleText element = new ConsoleText(original.element);
            LinkedHashMap<StyleElement, String> style = new LinkedHashMap<>();
            if (element.bold()) style.put(StyleElement.BOLD, "1");
            if (element.italic()) style.put(StyleElement.ITALIC, "3");
            if (element.underline()) style.put(StyleElement.UNDERLINE, "4");
            if (element.strikethrough()) style.put(StyleElement.STRIKETHROUGH, "9");
            if (element.subscript()) style.put(StyleElement.SCRIPT_SHIFT, "74");
            if (element.superscript()) style.put(StyleElement.SCRIPT_SHIFT, "73");
            if (element.element.contains("c")) {
                ObjectMap<String> map = element.element.getMap("c");
                int color = map.getInt("8", -1);
                if (color != -1) {
                    if (color < 8) {
                        style.put(StyleElement.FG_COLOR, String.valueOf(30 + color));
                    } else if (color < 16) {  //    90 + color - 8
                        style.put(StyleElement.FG_COLOR, String.valueOf(82 + color));
                    } else {
                        style.put(StyleElement.FG_COLOR, "38;5;" + color);
                    }
                } else {
                    int red = map.getInt("r");
                    int green = map.getInt("g");
                    int blue = map.getInt("b");
                    float alpha = map.getInt("a") / 255f;

                    red = Math.round(alpha * red);
                    green = Math.round(alpha * green);
                    blue = Math.round(alpha * blue);

                    style.put(StyleElement.FG_COLOR, "38;2;" + red + ";" + green + ";" + blue);
                }
            }
            if (element.element.contains("bc")) {
                ObjectMap<String> map = element.element.getMap("bc");
                int color = map.getInt("8", -1);
                if (color != -1) {
                    if (color < 8) {
                        style.put(StyleElement.BG_COLOR, String.valueOf(40 + color));
                    } else if (color < 16) {  //   100 + color - 8
                        style.put(StyleElement.BG_COLOR, String.valueOf(92 + color));
                    } else {
                        style.put(StyleElement.BG_COLOR, "48;5;" + color);
                    }
                } else {
                    int red = map.getInt("r");
                    int green = map.getInt("g");
                    int blue = map.getInt("b");
                    float alpha = map.getInt("a") / 255f;

                    red = Math.round(alpha * red);
                    green = Math.round(alpha * green);
                    blue = Math.round(alpha * blue);

                    style.put(StyleElement.BG_COLOR, "48;2;" + red + ";" + green + ";" + blue);
                }
            }

            // Remove Duplicate Style Elements
            if (existing.size() != 0) {
                StyleElement[] keys = style.keySet().toArray(new StyleElement[0]);
                for (StyleElement key : keys) {
                    if (existing.containsKey(key) && existing.get(key).equals(style.get(key))) {
                        style.remove(key);
                    }
                }
            }

            // Open Style Elements
            boolean sgr = style.size() != 0;
            if (sgr) {
                message.append("\u001B[");
                for (Iterator<String> i = style.values().iterator();;) {
                    message.append(i.next());
                    if (i.hasNext()) {
                        message.append(';');
                    } else {
                        message.append('m');
                        break;
                    }
                }
            }

            boolean hyperlink = false;
            if (element.onClick() != null) {
                String link = "\033]8;;" + element.onClick().toString() + "\007";
                if (existing.size() == 0 || !link.equals(existing.getOrDefault(StyleElement.HYPERLINK, null))) {
                    hyperlink = true;
                    style.put(StyleElement.HYPERLINK, link);
                    message.append(link);
                }
            }

            // Merge Metadata with Master
            LinkedHashMap<StyleElement, String> merged;
            if (existing.size() == 0) {
                merged = style;
            } else {
                merged = new LinkedHashMap<>(existing);
                merged.putAll(style);
            }

            // Write Prepend Queue
            try {
                for (TextElement e : original.prepend) message.append(convert(e, merged));
            } catch (Throwable e) {
                getLogger().error.println(e);
            }


            // Write Actual Text
            message.append(element.message());


            // Write Append Queue
            try {
                for (TextElement e : original.append) message.append(convert(e, merged));
            } catch (Throwable e) {
                getLogger().error.println(e);
            }


            // Close Style Elements
            boolean newline = master == null && message.toString().contains("\n");

            if (hyperlink) {
                message.append(existing.getOrDefault(StyleElement.HYPERLINK, "\033]8;;\007"));
            }

            if (sgr && !newline) {
                boolean individual = false;
                for (StyleElement key : existing.keySet()) if (key.reset != null) {
                    individual = true;
                    break;
                }

                if (individual) {
                    LinkedList<String> codes = new LinkedList<>();
                    for (StyleElement key : style.keySet()) {
                        if (key.reset != null && (!existing.containsKey(key) || !existing.get(key).equals(style.get(key)))) {
                            codes.add(existing.getOrDefault(key, key.reset));
                        }
                    }

                    if (codes.size() != 0) {
                        message.append("\u001B[");
                        for (Iterator<String> i = codes.iterator(); ; ) {
                            message.append(i.next());
                            if (i.hasNext()) {
                                message.append(';');
                            } else {
                                message.append('m');
                                break;
                            }
                        }
                    }
                } else {
                    message.append("\u001B[m");
                }
            }

            // Write After Queue
            try {
                for (TextElement e : original.after) message.append(convert(e));
            } catch (Throwable e) {
                getLogger().error.println(e);
            }

            return message.toString();
        } else {
            return "null";
        }
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
