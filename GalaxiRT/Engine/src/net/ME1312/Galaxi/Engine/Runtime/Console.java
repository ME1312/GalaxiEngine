package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Command.*;
import net.ME1312.Galaxi.Engine.CommandParser;
import net.ME1312.Galaxi.Event.Engine.CommandEvent;
import net.ME1312.Galaxi.Event.Engine.ConsoleInputEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Container.ContainedPair;
import net.ME1312.Galaxi.Library.Container.Pair;
import net.ME1312.Galaxi.Library.Try;
import net.ME1312.Galaxi.Library.Util;

import org.jline.reader.*;
import org.jline.terminal.TerminalBuilder;

import java.awt.*;
import java.io.IOError;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import static net.ME1312.Galaxi.Engine.GalaxiOption.*;

class Console extends CommandParser {
    volatile boolean jstatus;
    final LineReader jline;
    ConsoleUI window;
    final Thread thread;
    private final Parser parser;
    private final Engine engine;

    Console(Engine engine) throws Exception {
        this.engine = engine;

        TerminalBuilder jtb = TerminalBuilder.builder();
        if (!USE_JLINE.value()) jtb.dumb(true);
        if (!USE_ANSI.value()) jtb.jansi(false);
        this.jstatus = false;
        this.jline = LineReaderBuilder.builder()
                .appName(engine.getAppInfo().getName())
                .terminal(jtb.build())
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .option(LineReader.Option.AUTO_PARAM_SLASH, false)
                .option(LineReader.Option.AUTO_REMOVE_SLASH, false)
                .parser(this.parser = new Parser())
                .completer((reader, line, list) -> {
                    for (String s : Console.this.complete(ConsoleCommandSender.get(), (ParsedInput) line)) list.add(new Candidate(s));
                }).build();
        thread = new Thread(this::read, Galaxi.getInstance().getEngineInfo().getName() + "::Console_Reader");
        SystemLogger.start(this);
        try {
            if (SHOW_CONSOLE_WINDOW.value()) {
                openWindow(SHOW_CONSOLE_WINDOW.def());
            }
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.println(e);
        }
    }

    @Override
    protected ConsoleUI openWindow(boolean exit) {
        ConsoleUI window = this.window;
        if (window != null) {
            window.open();
        } else {
            if (!GraphicsEnvironment.isHeadless()) {
                this.window = window = Try.all.get(() -> (ConsoleUI) Class.forName("net.ME1312.Galaxi.Engine.Runtime.ConsoleWindow").getConstructor(Console.class, boolean.class).newInstance(this, exit));
                if (window != null) window.open();
            }
        }
        return window;
    }

    @Override
    protected ConsoleUI getWindow() {
        return window;
    }

    @Override
    protected void closeWindow(boolean destroy) {
        ConsoleUI window = this.window;
        if (window != null) {
            if (destroy) {
                this.window = null;
                window.destroy();
            } else {
                window.close();
            }
        }
    }

    static final Color[] ANSI_COLOR_MAP = new Color[] {
            new Color(  0,   0,   0),
            new Color(205,   0,   0),
            new Color( 37, 188,  36),
            new Color(215, 215,   0),
            new Color(  0,   0, 195),
            new Color(190,   0, 190),
            new Color(  0, 165, 220),
            new Color(204, 204, 204),

            new Color(128, 128, 128),
            new Color(255,   0,   0),
            new Color( 49, 231,  34),
            new Color(255, 255,   0),
            new Color(  0,   0, 255),
            new Color(255,   0, 255),
            new Color(  0, 200, 255),
            new Color(255, 255, 255),
    };
    static Color parse256(int color) {
        if (color < 16) {
            return ANSI_COLOR_MAP[color];
        } else if (color < 232) {
            float x = color - 16;
            int r = (int) (Math.floor(x / 36f) * 51);
                  x =                (x % 36f)      ;
            int g = (int) (Math.floor(x /  6f) * 51);
            int b = (int) (Math.floor(x %  6f) * 51);
            return new Color(r, g, b);
        } else if (color < 256) {
            int gray = (int) (10.2f * (color - 231));
            return new Color(gray, gray, gray);
        } else {
            throw new IllegalArgumentException("Invalid 8-bit color: " + color);
        }
    }

    @Override
    public List<String> complete(CommandSender sender, String command) {
        Util.nullpo(sender, command);
        return complete(sender, parseCommand(command));
    }

    @Override
    public List<String> complete(CommandSender sender, Parsed command) {
        Util.nullpo(sender, command);

        LinkedList<String> candidates = new LinkedList<String>();
        if (command != null && command.line().codePoints().count() > 0 && (!(command instanceof ParsedInput) || ((ParsedInput) command).isCommand())) {
            LinkedList<String> arguments = command.words();
            String label = arguments.getFirst();
            arguments.removeFirst();
            String[] args = arguments.toArray(new String[0]);

            TreeMap<String, Command> commands = engine.code.commands;

            if (command.wordIndex() <= 0) {
                if (label.length() > 0)
                    for (String handle : commands.keySet())
                        if (handle.startsWith(label.toLowerCase()))
                            candidates.add(handle);
            } else if (commands.containsKey(label.toLowerCase())) {
                CompletionHandler autocompletor = commands.get(label.toLowerCase()).autocomplete();
                if (autocompletor != null)
                    for (String autocomplete : autocompletor.complete(sender, label, args))
                        if (autocomplete != null && autocomplete.length() > 0)
                            candidates.add(autocomplete);
            }
        }
        return candidates;
    }

    private void read() {
        jstatus = true;
        try {
            boolean interrupted = false;
            do {
                try {
                    String line;
                    while (engine.running && (line = jline.readLine((USE_JLINE.value())?">":"")) != null) {
                        if (!engine.running || line.replaceAll("\\s", "").length() == 0) continue;
                        jstatus = false;
                        read(line);
                        jstatus = true;
                    }
                } catch (UserInterruptException e) {
                    if (!interrupted) {
                        interrupted = true;
                        new Thread(() -> {
                            engine.getAppInfo().getLogger().warn.println("Interrupt Received");
                            engine.stop();
                        }, Galaxi.getInstance().getEngineInfo().getName() + "::Process_Interrupt").start();
                    }
                }
            } while (engine.running);
        } catch (IOError e) {
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.println(e);
        }
        jstatus = false;
    }
    void read(String line) {
        Util.nullpo(line);

        ConsoleInputEvent ie = new ConsoleInputEvent(engine, line);
        engine.code.executeEvent(ie);
        if (!ie.isCancelled()) {
            ConsoleCommandSender sender = ConsoleCommandSender.get();
            if (sender instanceof InputSender && !line.startsWith("/")) {
                sender.chat(Util.unescapeJavaString(line));
            } else if (runCommand(sender, line) == Status.UNKNOWN) {
                engine.getAppInfo().getLogger().message.println("Unknown Command: " + ((line.startsWith("/"))?"":"/") + line);
            }
        }
    }

    @Override
    public Status runCommand(CommandSender sender, String command) {
        Util.nullpo(sender, command);
        return runCommand(sender, parseCommand(command));
    }

    @Override
    public Status runCommand(CommandSender sender, Parsed command) {
        Util.nullpo(sender, command);

        LinkedList<String> arguments = command.words();
        String label = arguments.getFirst();
        arguments.removeFirst();
        if (command.rawWordLength() <= 0) arguments.removeLast();
        String[] args = arguments.toArray(new String[0]);

        CommandEvent event = new CommandEvent(engine, sender, (!command.line().startsWith("/"))?command.line():command.line().substring(1), label, args);
        engine.code.executeEvent(event);
        if (!event.isCancelled()) {
            TreeMap<String, Command> commands = engine.code.commands;

            if (commands.containsKey(label.toLowerCase())) {
                try {
                    commands.get(label.toLowerCase()).command(sender, label, args);
                    return Status.SUCCESS;
                } catch (Exception e) {
                    engine.getAppInfo().getLogger().error.println(new InvocationTargetException(e, "Unhandled exception while running command"));
                    return Status.ERROR;
                }
            } else {
                return Status.UNKNOWN;
            }
        } else {
            return Status.CANCELLED;
        }
    }

    @Override
    public String escapeCommand(String label, String[] args, boolean literal, boolean whitespaced) {
        Util.nullpo(label, args);

        StringBuilder builder = new StringBuilder();
        builder.append('/');
        builder.append(escapeArguments(new String[]{ label }, literal, whitespaced));
        if (args.length > 0) {
            builder.append(' ');
            builder.append(escapeArguments(args, literal, whitespaced));
        }
        return builder.toString();
    }

    @Override
    public String escapeArguments(String[] args, boolean literal, boolean whitespaced) {
        Util.nullpo((Object) args);

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) builder.append(' ');
            builder.append(escapeArgument(null, args[i], literal, whitespaced, true));
        }
        return builder.toString();
    }

    private String escapeArgument(Pair<String, String> start, String arg, boolean literal, boolean whitespaced, boolean complete) {
        Util.nullpo((Object) arg);
        boolean append = start != null && arg.startsWith(start.value());
        if (append) arg = arg.substring(start.value().length());

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
            if (PARSE_CONSOLE_VARIABLES.value())
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
        if (append) arg = start.key() + arg;
        else if (whitespaced) arg = '\"' + arg;
        return arg;
    }

    @Override
    public ParsedInput parseCommand(String command) {
        Util.nullpo(command);
        return parser.parse(command, (int) command.codePoints().count(), true);
    }

    ParsedInput parse(String input, boolean command) {
        Util.nullpo(input);
        return parser.parse(input, (int) input.codePoints().count(), command);
    }

    ParsedInput parse(String input) {
        Util.nullpo(input);
        return parser.parse(input, (int) input.codePoints().count(), null);
    }

    private final class Parser implements org.jline.reader.Parser {
        @Override
        public ParsedInput parse(final String LINE, final int CURSOR, ParseContext ctx) throws SyntaxError {
            return parse(LINE, CURSOR, !(ConsoleCommandSender.get() instanceof InputSender));
        }

        public ParsedInput parse(final String LINE, final int CURSOR, boolean command) throws SyntaxError {
            final LinkedList<ContainedPair<String, String>> MAPPINGS = new LinkedList<ContainedPair<String, String>>();

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
                                MAPPINGS.add(new ContainedPair<>(LINE.substring(start, i), part.toString()));
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
                                    if ((PARSE_CONSOLE_VARIABLES.value())
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
                                    if ((PARSE_CONSOLE_VARIABLES.value())
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

            MAPPINGS.add(new ContainedPair<>(LINE.substring(start), WORD));
            return new ParsedInput() {
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
                    for (Pair<String, String> e : MAPPINGS) list.add(e.value());
                    return list;
                }

                @Override
                public int wordCursor() {
                    return WORD_CURSOR;
                }

                @Override
                public int wordIndex() {
                    return (MAPPINGS.size() == 0)?0:MAPPINGS.size() - 1;
                }

                @Override
                public Pair<String, String> translation() {
                    return (MAPPINGS.size() == 0)?null:MAPPINGS.getLast();
                }

                @Override
                public LinkedList<Pair<String, String>> translations() {
                    return new LinkedList<>(MAPPINGS);
                }

                @Override
                public String rawWord() {
                    return RAW_WORD;
                }

                @Override
                public LinkedList<String> rawWords() {
                    LinkedList<String> list = new LinkedList<>();
                    for (Pair<String, String> e : MAPPINGS) list.add(e.key());
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

    interface ParsedInput extends Parsed, CompletingParsedLine {
        boolean isCommand();
    }
}
