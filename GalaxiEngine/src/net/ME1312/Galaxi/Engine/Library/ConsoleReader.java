package net.ME1312.Galaxi.Engine.Library;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.Library.Log.SystemLogger;
import net.ME1312.Galaxi.Event.ConsoleChatEvent;
import net.ME1312.Galaxi.Event.CommandEvent;
import net.ME1312.Galaxi.Event.ConsoleInputEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Callback.Callback;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.Command.Command;
import net.ME1312.Galaxi.Plugin.Command.CommandSender;
import net.ME1312.Galaxi.Plugin.Command.CompletionHandler;
import net.ME1312.Galaxi.Plugin.Command.ConsoleCommandSender;
import net.ME1312.Galaxi.Plugin.PluginManager;
import org.jline.reader.*;
import org.jline.terminal.TerminalBuilder;

import java.awt.*;
import java.io.IOError;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import static net.ME1312.Galaxi.Engine.GalaxiOption.*;

/**
 * Console Reader Class
 */
public class ConsoleReader {
    private Container<Boolean> running;
    private LineReader jline;
    private Parser parser;
    private Thread thread;
    private OutputStream window;
    private Callback<String> chat = null;
    private GalaxiEngine engine;

    /**
     * Create a ConsoleReader
     *
     * @param engine GalaxiEngine
     * @param status Status Container
     */
    public ConsoleReader(GalaxiEngine engine, Container<Boolean> status) throws Exception {
        this.engine = engine;
        this.running = status;

        TerminalBuilder jtb = TerminalBuilder.builder();
        if (!USE_JLINE.def()) jtb.dumb(true);
        if (!USE_ANSI.def()) jtb.jansi(false);
        this.jline = LineReaderBuilder.builder()
                .appName(engine.getAppInfo().getName())
                .terminal(jtb.build())
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .option(LineReader.Option.AUTO_PARAM_SLASH, false)
                .option(LineReader.Option.AUTO_REMOVE_SLASH, false)
                .parser(this.parser = new Parser())
                .completer((reader, line, list) -> {
                    for (String s : ConsoleReader.this.complete(ConsoleCommandSender.get(), (ParsedCommand) line)) list.add(new Candidate(s));
                }).build();
        thread = new Thread(this::read, Galaxi.getInstance().getEngineInfo().getName() + "::Console_Reader");
        Util.reflect(SystemLogger.class.getDeclaredMethod("start", LineReader.class), null, jline);
        try {
            if (SHOW_CONSOLE_WINDOW.usr().equalsIgnoreCase("true") || (SHOW_CONSOLE_WINDOW.usr().length() <= 0 && SHOW_CONSOLE_WINDOW.get() && System.console() == null)) {
                openConsoleWindow(true);
            }
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.println(e);
        }
    }

    /**
     * Set a chat listener (commands not starting with <b>/</b> will become chat messages)
     *
     * @param listener Chat Listener
     */
    public void setChatListener(Callback<String> listener) {
        this.chat = listener;
    }

    /**
     * Open the Console Window
     *
     * @param exit Whether to exit when the user closes the window
     */
    public void openConsoleWindow(boolean exit) {
        if (!GraphicsEnvironment.isHeadless())
            window = Util.getDespiteException(() -> (OutputStream) Class.forName("net.ME1312.Galaxi.Engine.Standalone.ConsoleWindow").getConstructor(ConsoleReader.class, boolean.class).newInstance(this, exit), null);
    }

    /**
     * Close the Console Window
     */
    public void closeConsoleWindow() {
        if (window != null) Util.isException(() -> window.close());
        window = null;
    }

    /**
     * Complete a command
     *
     * @param command Command
     * @return Auto Completions
     */
    public List<String> complete(CommandSender sender, String command) {
        if (Util.isNull(sender, command)) throw new NullPointerException();
        return complete(sender, parseCommand(command));
    }

    /**
     * Complete a command
     *
     * @param command Parsed Command
     * @return Auto Completions
     */
    public List<String> complete(CommandSender sender, ParsedCommand command) {
        if (Util.isNull(sender, command)) throw new NullPointerException();

        LinkedList<String> candidates = new LinkedList<String>();
        if (command != null && command.line().codePoints().count() > 0 && (chat == null || command.isCommand())) {
            LinkedList<String> arguments = command.words();
            String label = arguments.getFirst();
            arguments.removeFirst();
            String[] args = arguments.toArray(new String[0]);

            TreeMap<String, Command> commands;
            try {
                commands = Util.reflect(PluginManager.class.getDeclaredField("commands"), engine.getPluginManager());
            } catch (Exception e) {
                e.printStackTrace();
                commands = new TreeMap<String, Command>();
            }

            if (command.wordIndex() <= 0) {
                if (label.length() > 0)
                    for (String handle : commands.keySet())
                        if (handle.startsWith(label.toLowerCase()))
                            candidates.add(handle);
            } else if (commands.keySet().contains(label.toLowerCase())) {
                CompletionHandler autocompletor = commands.get(label.toLowerCase()).autocomplete();
                if (autocompletor != null)
                    for (String autocomplete : autocompletor.complete(sender, label, args))
                        if (!Util.isNull(autocomplete) && autocomplete.length() > 0)
                            candidates.add(autocomplete);
            }
        }
        return candidates;
    }

    private void read() {
        try {
            String line;
            while (running.get() && (line = jline.readLine((USE_JLINE.def())?">":"")) != null) {
                if (!running.get() || line.replaceAll("\\s", "").length() == 0) continue;
                input(line);
            }
        } catch (IOError e) {
        } catch (UserInterruptException e) {
            engine.getAppInfo().getLogger().warn.println("Interrupt Received");
            engine.stop();
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.println(e);
        }
    }
    private void input(String line) {
        if (Util.isNull(line)) throw new NullPointerException();

        ConsoleInputEvent ie = new ConsoleInputEvent(engine, line);
        engine.getPluginManager().executeEvent(ie);
        if (!ie.isCancelled()) {
            if (chat != null && !line.startsWith("/")) {
                chat(Util.unescapeJavaString(line));
            } else {
                runCommand(ConsoleCommandSender.get(), line);
            }
        }
    }

    /**
     * Send a chat message as Console
     *
     * @param message Message
     */
    public void chat(String message) {
        if (Util.isNull(message)) throw new NullPointerException();
        if (Util.isNull(chat)) throw new IllegalStateException("No handler available for console chat messages");
        try {
            ConsoleChatEvent event = new ConsoleChatEvent(engine, message);
            engine.getPluginManager().executeEvent(event);
            if (!event.isCancelled()) chat.run(event.getMessage());
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.print(e);
        }
    }

    /**
     * Run a command
     *
     * @param sender Command Sender
     * @param command Command
     */
    public void runCommand(CommandSender sender, String command) {
        if (Util.isNull(sender, command)) throw new NullPointerException();
        runCommand(sender, parseCommand(command));
    }

    /**
     * Run a command
     *
     * @param sender Command Sender
     * @param command Parsed Command
     */
    @SuppressWarnings("unchecked")
    public void runCommand(CommandSender sender, ParsedCommand command) {
        if (Util.isNull(sender, command)) throw new NullPointerException();

        LinkedList<String> arguments = command.words();
        System.out.println(arguments);
        String label = arguments.getFirst();
        arguments.removeFirst();
        if (command.rawWordLength() <= 0) arguments.removeLast();
        String[] args = arguments.toArray(new String[0]);

        CommandEvent event = new CommandEvent(engine, sender, (!command.line().startsWith("/"))?command.line():command.line().substring(1), label, args);
        engine.getPluginManager().executeEvent(event);
        if (!event.isCancelled()) {
            TreeMap<String, Command> commands;
            try {
                commands = Util.reflect(PluginManager.class.getDeclaredField("commands"), engine.getPluginManager());
            } catch (Exception e) {
                e.printStackTrace();
                commands = new TreeMap<String, Command>();
            }

            if (commands.keySet().contains(label.toLowerCase())) {
                try {
                    commands.get(label.toLowerCase()).command(sender, label, args);
                } catch (Exception e) {
                    engine.getAppInfo().getLogger().error.println(new InvocationTargetException(e, "Uncaught exception while running command"));
                }
            } else {
                engine.getAppInfo().getLogger().message.println("Unknown Command: " + escapeCommand(label, args));
            }
        }
    }

    /**
     * Escapes a command
     *
     * @param label Command Label
     * @param args Command Arguments
     * @return Escaped Command
     */
    public String escapeCommand(String label, String... args) {
        return escapeCommand(label, args, false, false);
    }

    /**
     * Escapes some arguments
     *
     * @param args Command Arguments
     * @return Escaped Arguments
     */
    public String escapeArguments(String... args) {
        return escapeArguments(args, false, false);
    }

    /**
     * Escapes a command
     *
     * @param label Command Label
     * @param args Command Arguments
     * @param literal Literal String Escape Mode (using Single Quotes)
     * @param whitespaced Whitespaced String Escape Mode (using Double Quotes)
     * @return Escaped Command
     */
    public String escapeCommand(String label, String[] args, boolean literal, boolean whitespaced) {
        if (Util.isNull(label, args)) throw new NullPointerException();

        StringBuilder builder = new StringBuilder();
        builder.append('/');
        builder.append(escapeArguments(new String[]{ label }, literal, whitespaced));
        if (args.length > 0) {
            builder.append(' ');
            builder.append(escapeArguments(args, literal, whitespaced));
        }
        return builder.toString();
    }

    /**
     * Escapes some arguments
     *
     * @param args Command Arguments
     * @param literal Literal String Escape Mode (using Single Quotes)
     * @param whitespaced Whitespaced String Escape Mode (using Double Quotes)
     * @return Escaped Arguments
     */
    public String escapeArguments(String[] args, boolean literal, boolean whitespaced) {
        if (Util.isNull((Object) args)) throw new NullPointerException();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) builder.append(' ');
            builder.append(escapeArgument(null, args[i], literal, whitespaced, true));
        }
        return builder.toString();
    }

    private String escapeArgument(NamedContainer<String, String> start, String arg, boolean literal, boolean whitespaced, boolean complete) {
        if (Util.isNull((Object) arg)) throw new NullPointerException();
        boolean append = start != null && arg.startsWith(start.get());
        if (append) arg = arg.substring(start.get().length());

        if (literal) {
            if (!append)
                arg = arg + '\'';
            arg = arg.replace("\'", "\'\\\'\'");
            if (complete) {
                arg += '\'';
                if (whitespaced)
                    arg += '\"';
            }
        } else {
            arg = arg.replace("\\", "\\\\").replace("\n", "\\n").replace("\'", "\\\'").replace("\"", "\\\"");
            if (PARSE_CONSOLE_VARIABLES.usr().equalsIgnoreCase("true") || (PARSE_CONSOLE_VARIABLES.usr().length() <= 0 && PARSE_CONSOLE_VARIABLES.get()))
                arg = arg.replace("$", "\\$").replace("%", "\\%");
            if (!whitespaced) {
                if (append || arg.length() > 0) {
                    arg = arg.replace(" ", "\\ ");
                } else {
                    arg = "\"\"";
                }
            } else if (complete) {
                arg += '\"';
            }
        }
        if (append) arg = start.name() + arg;
        else if (whitespaced) arg = '\"' + arg;
        return arg;
    }

    /**
     * Parses a command
     *
     * @param command Command
     * @return Parsed Command
     */
    public ParsedCommand parseCommand(String command) {
        if (Util.isNull(command)) throw new NullPointerException();
        return parser.parse(command, (int) command.codePoints().count(), true);
    }

    private ParsedCommand parse(String command) {
        if (Util.isNull(command)) throw new NullPointerException();
        return parser.parse(command, (int) command.codePoints().count(), false);
    }

    private final class Parser implements org.jline.reader.Parser {
        @Override
        public ParsedCommand parse(final String LINE, final int CURSOR, ParseContext ctx) throws SyntaxError {
            return parse(LINE, CURSOR, false);
        }

        public ParsedCommand parse(final String LINE, final int CURSOR, boolean command) throws SyntaxError {
            if (!command) command = chat == null;
            final LinkedList<NamedContainer<String, String>> MAPPINGS = new LinkedList<NamedContainer<String, String>>();

            StringBuilder part = new StringBuilder();
            int wcursor = 0;
            int rwcursor = 0;
            int start = 0;

            boolean between = true;
            boolean literal = false;
            boolean whitespaced = false;
            for (int i = 0; i < LINE.codePoints().count(); i++) {
                int ch = LINE.codePointAt(i);
                if (CURSOR > i) rwcursor++;
                if (i == 0 && ch == '/') { // This has been defined as a command
                    command = true;
                } else if (!command && i <= 0) { // This must be a chat message, stop parsing
                    wcursor = CURSOR;
                    rwcursor = CURSOR;
                    part.append(LINE);
                    break;
                } else {
                    if (ch == '\'') { // Begin, end, or skip a literal block
                        if (literal && i + 1 < LINE.codePoints().count() && LINE.codePointAt(i + 1) == '\'') {
                            part.appendCodePoint(ch);
                            if (CURSOR > i) wcursor++;
                        }
                        literal = !literal;
                    } else if (literal) { // Accept characters literally
                        if (CURSOR > i) wcursor++;
                        part.appendCodePoint(ch);
                        between = false;
                    } else {
                        if (!whitespaced && ch == ' ') {
                            if (!between) { // Ends the current word
                                MAPPINGS.add(new NamedContainer<>(LINE.substring(start, i), part.toString()));
                                part = new StringBuilder(LINE.length());
                            }
                            start = i + 1;
                            wcursor = 0;
                            rwcursor = 0;
                            between = true;
                        } else {
                            between = false;
                            switch (ch) {
                                case '\"': // Begin, end, or skip a whitespaced block
                                    if (whitespaced && i + 1 < LINE.codePoints().count() && LINE.codePointAt(i + 1) == '\"') {
                                        part.appendCodePoint(ch);
                                        if (CURSOR > i) wcursor++;
                                    }
                                    whitespaced = !whitespaced;
                                    continue;
                                case '$': // Replace java system variables
                                    int varEnd;
                                    if ((PARSE_CONSOLE_VARIABLES.usr().equalsIgnoreCase("true") || (PARSE_CONSOLE_VARIABLES.usr().length() <= 0 && PARSE_CONSOLE_VARIABLES.get()))
                                            && i + 1 <= LINE.codePoints().count() && (varEnd = LINE.indexOf('$', i+1)) > i) {
                                        String var = LINE.substring(i + 1, varEnd);
                                        String replacement;
                                        if (System.getProperty(var) != null) {
                                            replacement = System.getProperty(var);
                                        } else {
                                            replacement = "null";
                                        }
                                        part.append(replacement);

                                        int offset = varEnd - i;
                                        if (CURSOR > varEnd) wcursor += replacement.codePoints().count();
                                        for (int x = 0; x < offset; x++) {
                                            i++;
                                            if (CURSOR > i) rwcursor++;
                                        }
                                    } else {
                                        part.appendCodePoint(ch);
                                        if (CURSOR > i) wcursor++;
                                    }
                                    continue;
                                case '%': // Replace environment variables
                                    if ((PARSE_CONSOLE_VARIABLES.usr().equalsIgnoreCase("true") || (PARSE_CONSOLE_VARIABLES.usr().length() <= 0 && PARSE_CONSOLE_VARIABLES.get()))
                                            && i + 1 <= LINE.codePoints().count() && (varEnd = LINE.indexOf('%', i+1)) > i) {
                                        String var = LINE.substring(i + 1, varEnd);
                                        String replacement;
                                        if (System.getenv(var) != null) {
                                            replacement = System.getenv(var);
                                        } else {
                                            replacement = "null";
                                        }
                                        part.append(replacement);

                                        int offset = varEnd - i;
                                        if (CURSOR > varEnd) wcursor += replacement.codePoints().count();
                                        for (int x = 0; x < offset; x++) {
                                            i++;
                                            if (CURSOR > i) rwcursor++;
                                        }
                                    } else {
                                        part.appendCodePoint(ch);
                                        if (CURSOR > i) wcursor++;
                                    }
                                    continue;
                                case '\\': // Parse escape sequences
                                    int nextChar = (i == LINE.codePoints().count() - 1) ? '\\' : LINE
                                            .codePointAt(i + 1);
                                    // Octal Escape: 000
                                    if (nextChar >= '0' && nextChar <= '7') {
                                        StringBuilder code = new StringBuilder();
                                        code.appendCodePoint(nextChar);
                                        i++;
                                        if (CURSOR > i) rwcursor++;
                                        if ((i < LINE.codePoints().count() - 1) && LINE.codePointAt(i + 1) >= '0'
                                                && LINE.codePointAt(i + 1) <= '7') {
                                            code.appendCodePoint(LINE.codePointAt(i + 1));
                                            i++;
                                            if (CURSOR > i) rwcursor++;
                                            if ((i < LINE.codePoints().count() - 1) && LINE.codePointAt(i + 1) >= '0'
                                                    && LINE.codePointAt(i + 1) <= '7') {
                                                code.appendCodePoint(LINE.codePointAt(i + 1));
                                                i++;
                                                if (CURSOR > i) rwcursor++;
                                            }
                                        }
                                        part.append((char) Integer.parseInt(code.toString(), 8));
                                        if (CURSOR > i) wcursor++;
                                        continue;
                                    } else switch (nextChar) {
                                        case '\\':
                                            ch = '\\';
                                            break;
                                        case ' ':
                                            ch = ' ';
                                            break;
                                        case '$':
                                            ch = '$';
                                            break;
                                        case '%':
                                            ch = '%';
                                            break;
                                        case 'b':
                                            ch = '\b';
                                            break;
                                        case 'f':
                                            ch = '\f';
                                            break;
                                        case 'n':
                                            ch = '\n';
                                            break;
                                        case 'r':
                                            ch = '\r';
                                            break;
                                        case 't':
                                            ch = '\t';
                                            break;
                                        case '\"':
                                            ch = '\"';
                                            break;
                                        case '\'':
                                            ch = '\'';
                                            break;
                                        // Unicode Java Escape: u0000
                                        // Unicode ES-6 Escape: u{000000}
                                        case 'u':
                                            try {
                                                if (i >= LINE.codePoints().count() - 4) throw new IllegalStateException();
                                                StringBuilder escape = new StringBuilder();
                                                int offset = 2;

                                                if (LINE.codePointAt(i + 2) != '{') {
                                                    if (i >= LINE.codePoints().count() - 5) throw new IllegalStateException();
                                                    while (offset <= 5) {
                                                        Integer.toString(LINE.codePointAt(i + offset), 16);
                                                        escape.appendCodePoint(LINE.codePointAt(i + offset));
                                                        offset++;
                                                    }
                                                    offset--;
                                                } else {
                                                    offset++;
                                                    while (LINE.codePointAt(i + offset) != '}') {
                                                        Integer.toString(LINE.codePointAt(i + offset), 16);
                                                        escape.appendCodePoint(LINE.codePointAt(i + offset));
                                                        offset++;
                                                    }
                                                }
                                                ch = Integer.parseInt(escape.toString(), 16);

                                                for (int x = 0; x < offset - 1; x++) {
                                                    i++;
                                                    if (CURSOR > i) rwcursor++;
                                                }
                                            } catch (Throwable e) {
                                                ch = 'u';
                                            }
                                            break;
                                    }
                                    i++;
                                    if (CURSOR > i) rwcursor++;
                                default:
                                    if (CURSOR > i) wcursor++;
                                    part.appendCodePoint(ch);
                                //  continue;
                            }
                        }
                    }
                }
            }

            // Construct Data Element

            final String WORD = part.toString();
            final String RAW_WORD = LINE.substring(start);
            final int WORD_CURSOR = wcursor;
            final int RAW_WORD_CURSOR = rwcursor;

            final boolean COMMAND = command;
            final boolean LITERAL = literal;
            final boolean WHITESPACED = whitespaced;

            MAPPINGS.add(new NamedContainer<>(LINE.substring(start), WORD));
            return new ParsedCommand() {
                @Override
                public CharSequence escape(CharSequence argument, boolean complete) {
                    return escapeArgument(translation(), argument.toString(), LITERAL, WHITESPACED, complete);
                }

                @Override
                public boolean isCommand() {
                    return COMMAND;
                }

                @Override
                public String word() {
                    return WORD;
                }

                @Override
                public LinkedList<String> words() {
                    LinkedList<String> list = new LinkedList<>();
                    for (NamedContainer<String, String> e : MAPPINGS) list.add(e.get());
                    return list;
                }

                @Override
                public int wordCursor() {
                    return WORD_CURSOR;
                }

                @Override
                public int wordIndex() {
                    return (MAPPINGS.size() <= 0)?0:MAPPINGS.size() - 1;
                }

                @Override
                public NamedContainer<String, String> translation() {
                    return (MAPPINGS.size() <= 0)?null:MAPPINGS.getLast();
                }

                @Override
                public LinkedList<NamedContainer<String, String>> translations() {
                    return new LinkedList<>(MAPPINGS);
                }

                @Override
                public String rawWord() {
                    return RAW_WORD;
                }

                @Override
                public LinkedList<String> rawWords() {
                    LinkedList<String> list = new LinkedList<>();
                    for (NamedContainer<String, String> e : MAPPINGS) list.add(e.name());
                    return list;
                }

                @Override
                public int rawWordCursor() {
                    return RAW_WORD_CURSOR;
                }

                @Override
                public int rawWordLength() {
                    return RAW_WORD.length();
                }

                @Override
                public String line() {
                    return LINE;
                }

                @Override
                public int cursor() {
                    return CURSOR;
                }
            };
        }
    }

    /**
     * Parsed Command Results Class
     */
    public interface ParsedCommand extends CompletingParsedLine {
        /**
         * Get the Word List
         *
         * @return Word List
         */
        @Override
        LinkedList<String> words();

        /**
         * Get the Word Translation
         *
         * @return Word Translation
         */
        NamedContainer<String, String> translation();

        /**
         * Get the Word Translation List
         *
         * @return Word Translation List
         */
        LinkedList<NamedContainer<String, String>> translations();

        /**
         * Get the Raw Word
         *
         * @return Raw Word
         */
        String rawWord();

        /**
         * Get the Raw Word List
         *
         * @return Raw Word List
         */
        LinkedList<String> rawWords();

        /**
         * Get if this was defined as a command
         *
         * @return Command Status
         */
        boolean isCommand();
    }
}
