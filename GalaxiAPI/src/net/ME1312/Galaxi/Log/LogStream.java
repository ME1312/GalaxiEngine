package net.ME1312.Galaxi.Log;

import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Try;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

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
        this.primitive = Try.none.get(() -> new PrintStream(new OutputStream() {
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
        }, true, UTF_8.name()));
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
        StringBuilder result = new StringBuilder();
        render(new LinkedList<>(), original, null, null, result);
        return result.toString();
    }

    private static final class TextState {
        private final Map<StyleElement, String> style;
        private String hyperlink = null;

        private TextState() {
            style = new LinkedHashMap<>();
        }

        private TextState(TextState other) {
            style = new LinkedHashMap<>(other.style);
            hyperlink = other.hyperlink;
        }
    }

    private enum StyleElement {
        BOLD("22"),
        ITALIC("23"),
        UNDERLINE("24"),
        STRIKETHROUGH("29"),
        FG_COLOR("39"),
        BG_COLOR("49"),
        SCRIPT_SHIFT("75"),
        ;
        private final String reset;
        StyleElement(String reset) {
            this.reset = reset;
        }
    }

    @SuppressWarnings("unchecked")
    private void render(LinkedList<TextElement> stack, TextElement original, TextState parent, TextState current, StringBuilder result) {
        if (stack.contains(original)) {
            getLogger().error.println(new IllegalStateException("Infinite text loop"));
            return;
        }
        stack.add(original);

        try {
            if (original != null) {
                boolean top = current == null;
                if (top) current = new TextState();

                // Write Before Queue
                for (TextElement e : original.before) if (e != null) {
                    render(stack, e, null, current, result);
                }

                // Calculate Style/Meta Elements
                ConsoleText element = new ConsoleText(original.element);
                TextState text = (parent == null)? new TextState() : new TextState(parent);
                if (element.element.contains("bc")) {
                    ObjectMap<String> map = element.element.getMap("bc");
                    int color = map.getInt("8", -1);
                    if (color != -1) {
                        if (color < 8) {
                            text.style.put(StyleElement.BG_COLOR, String.valueOf(40 + color));
                        } else if (color < 16) {  //   100 + color - 8
                            text.style.put(StyleElement.BG_COLOR, String.valueOf(92 + color));
                        } else {
                            text.style.put(StyleElement.BG_COLOR, "48;5;" + color);
                        }
                    } else {
                        float alpha = map.getInt("a") / 255f;
                        int red = Math.round(alpha * map.getInt("r"));
                        int green = Math.round(alpha * map.getInt("g"));
                        int blue = Math.round(alpha * map.getInt("b"));

                        text.style.put(StyleElement.BG_COLOR, "48;2;" + red + ";" + green + ";" + blue);
                    }
                }
                if (element.element.contains("c")) {
                    ObjectMap<String> map = element.element.getMap("c");
                    int color = map.getInt("8", -1);
                    if (color != -1) {
                        if (color < 8) {
                            text.style.put(StyleElement.FG_COLOR, String.valueOf(30 + color));
                        } else if (color < 16) {  //    90 + color - 8
                            text.style.put(StyleElement.FG_COLOR, String.valueOf(82 + color));
                        } else {
                            text.style.put(StyleElement.FG_COLOR, "38;5;" + color);
                        }
                    } else {
                        float alpha = map.getInt("a") / 255f;
                        int red = Math.round(alpha * map.getInt("r"));
                        int green = Math.round(alpha * map.getInt("g"));
                        int blue = Math.round(alpha * map.getInt("b"));

                        text.style.put(StyleElement.FG_COLOR, "38;2;" + red + ";" + green + ";" + blue);
                    }
                }
                if (element.bold()) text.style.put(StyleElement.BOLD, "1");
                if (element.italic()) text.style.put(StyleElement.ITALIC, "3");
                if (element.underline()) text.style.put(StyleElement.UNDERLINE, "4");
                if (element.strikethrough()) text.style.put(StyleElement.STRIKETHROUGH, "9");
                if (element.subscript()) text.style.put(StyleElement.SCRIPT_SHIFT, "74");
                if (element.superscript()) text.style.put(StyleElement.SCRIPT_SHIFT, "73");
                if (element.onClick() != null) text.hyperlink = element.onClick().toString();

                // Write Prepend Queue
                for (TextElement e : original.prepend) if (e != null) {
                    render(stack, e, text, current, result);
                }


                // Recalculate Style Elements
                if (element.message() != null && element.message().length() != 0) {
                    Collection<String> sgr;
                    if (current.style.size() == 0) {
                        sgr = text.style.values();
                        current.style.putAll(text.style);
                    } else {
                        sgr = new LinkedList<>();
                        StyleElement[] open = current.style.keySet().toArray(new StyleElement[0]);
                        for (int i = open.length; i > 0;) {
                            StyleElement key = open[--i];
                            if (!text.style.containsKey(key)) {
                                sgr.add(key.reset);
                                current.style.remove(key);
                            }
                        }

                        if (current.style.size() == 0) {
                            sgr.clear();
                            if (text.style.size() == 0) {
                                sgr.add("");
                            } else {
                                sgr.add("0");
                                sgr.addAll(text.style.values());
                                current.style.putAll(text.style);
                            }
                        } else {
                            Map.Entry<StyleElement, String>[] entries = text.style.entrySet().toArray(new Map.Entry[0]);
                            for (Map.Entry<StyleElement, String> entry : entries) {
                                if (!current.style.containsKey(entry.getKey()) || !current.style.get(entry.getKey()).equals(entry.getValue())) {
                                    sgr.add(entry.getValue());
                                    current.style.put(entry.getKey(), entry.getValue());
                                }
                            }
                        }
                    }

                    // Open Style Elements
                    if (sgr.size() != 0) {
                        result.append("\u001B[");
                        for (Iterator<String> i = sgr.iterator();;) {
                            result.append(i.next());
                            if (i.hasNext()) {
                                result.append(';');
                            } else {
                                result.append('m');
                                break;
                            }
                        }
                    }

                    // Open Meta Elements
                    if (text.hyperlink != null) {
                        if (!text.hyperlink.equals(current.hyperlink)) {
                            current.hyperlink = text.hyperlink;
                            result.append("\033]8;;").append(text.hyperlink).append('\007');
                        }
                    } else if (current.hyperlink != null) {
                        current.hyperlink = null;
                        result.append("\033]8;;\007");
                    }


                    // Write Actual Text
                    result.append(element.message());
                }


                // Write Append Queue
                for (TextElement e : original.append) if (e != null) {
                    render(stack, e, text, current, result);
                }

                // Write After Queue
                for (TextElement e : original.after) if (e != null) {
                    render(stack, e, null, current, result);
                }

                // Close Remaining Style/Meta Elements
                if (top) {
                    boolean newline = result.length() != 0 && result.codePointAt(result.codePointCount(0, result.length()) - 1) == '\n';
                    Consumer<String> insert = (newline)? s -> result.insert(result.length() - 1, s) : result::append;

                    if (current.hyperlink != null) insert.accept("\033]8;;\007");
                    if (!newline && current.style.size() != 0) result.append("\u001B[m");
                }
            } else {
                result.append("null");
            }
        } catch (Exception e) {
            getLogger().error.println(e);
        } finally {
            stack.removeLast();
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
