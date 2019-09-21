package net.ME1312.Galaxi.Engine.Library;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.Library.Log.SystemLogger;
import net.ME1312.Galaxi.Event.ConsoleChatEvent;
import net.ME1312.Galaxi.Event.CommandEvent;
import net.ME1312.Galaxi.Event.ConsoleInputEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Callback.Callback;
import net.ME1312.Galaxi.Library.Container;
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
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.List;

import static net.ME1312.Galaxi.Engine.GalaxiOption.*;

/**
 * Console Reader Class
 */
public class ConsoleReader extends Thread {
    private Container<Boolean> running;
    private LineReader jline;
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
        super(Galaxi.getInstance().getEngineInfo().getName() + "::Console_Reader");
        this.engine = engine;
        this.running = status;

        TerminalBuilder jtb = TerminalBuilder.builder();
        if (!USE_JLINE.def()) jtb.dumb(true);
        if (!USE_ANSI.def()) jtb.jansi(false);
        this.jline = LineReaderBuilder.builder()
                .appName(engine.getAppInfo().getName())
                .terminal(jtb.build())
                .completer((reader, line, list) -> {
                    for (String s : ConsoleReader.this.complete(line.line())) list.add(new Candidate(s));
                }).build();
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
            window = Util.getDespiteException(() -> (OutputStream) Class.forName("net.ME1312.Galaxi.Engine.Standalone.ConsoleWindow").getConstructor(Object.class, boolean.class).newInstance(this, exit), null);
    }

    /**
     * Close the Console Window
     */
    public void closeConsoleWindow() {
        if (window != null) Util.isException(() -> window.close());
        window = null;
    }

    private List<String> complete(String command, boolean full) {
        LinkedList<String> candidates = new LinkedList<String>();
        if (command != null && command.length() > 0 && (chat == null || command.startsWith("/"))) {
            LinkedList<Map.Entry<String, String>> args = new LinkedList<Map.Entry<String, String>>();
            args.addAll(translateCommand(command, true).entrySet());
            Map.Entry<String, String> cmd = args.getFirst();
            args.removeFirst();

            StringBuilder before = new StringBuilder();
            before.append(cmd.getKey());
            for (Map.Entry<String, String> entry : args) if (entry != args.getLast()) {
                before.append(' ');
                before.append(entry.getKey());
            }
            if (cmd.getValue().startsWith("/")) cmd.setValue(cmd.getValue().substring(1));

            TreeMap<String, Command> commands;
            try {
                commands = Util.reflect(PluginManager.class.getDeclaredField("commands"), engine.getPluginManager());
            } catch (Exception e) {
                e.printStackTrace();
                commands = new TreeMap<String, Command>();
            }

            if (args.size() == 0) {
                if (cmd.getValue().length() > 0)
                    for (String handle : commands.keySet())
                        if (handle.startsWith(cmd.getValue().toLowerCase()))
                            candidates.add(((command.startsWith("/"))?"/":"") + escapeCommand(handle));
            } else if (commands.keySet().contains(cmd.getValue().toLowerCase())) {
                CompletionHandler autocompletor = commands.get(cmd.getValue().toLowerCase()).autocomplete();
                String beginning = before.toString();
                String[] arguments = new String[args.size()];
                for (int i = 0; i < args.size(); i++) arguments[i] = args.get(i).getValue();
                if (autocompletor != null)
                    for (String autocomplete : autocompletor.complete(ConsoleCommandSender.get(), cmd.getValue(), arguments))
                        if (!Util.isNull(autocomplete) && autocomplete.length() > 0)
                            candidates.add(((full)?beginning+' ':"") + escapeCommand(autocomplete));
            }
        }
        return candidates;
    }

    /**
     * Complete a command
     *
     * @param command Command
     * @return Auto Completions (as Single Arguments)
     */
    public List<String> complete(String command) {
        return complete(command, false);
    }

    /**
     * Complete a command
     *
     * @param command Command
     * @return Auto Completions (as Full Lines)
     */
    public List<String> completeLine(String command) {
        return complete(command, true);
    }

    /**
     * Start the ConsoleReader loop
     */
    public void run() {
        try {
            String line;
            while (running.get() && (line = jline.readLine((USE_JLINE.def())?">":"")) != null) {
                if (!running.get() || line.replaceAll("\\s", "").length() == 0) continue;
                input(line);
            }
        } catch (IOError e) {
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
                try {
                    ConsoleChatEvent event = new ConsoleChatEvent(engine, Util.unescapeJavaString(line));
                    engine.getPluginManager().executeEvent(event);
                    if (!event.isCancelled()) chat.run(event.getMessage());
                } catch (Exception e) {
                    engine.getAppInfo().getLogger().error.print(e);
                }
            } else {
                runCommand(ConsoleCommandSender.get(), line);
            }
        }
    }

    /**
     * Run a command
     *
     * @param sender Command Sender
     * @param command Command
     */
    @SuppressWarnings("unchecked")
    public void runCommand(CommandSender sender, String command) {
        if (Util.isNull(sender, command)) throw new NullPointerException();
        if (command.startsWith("/")) command = command.substring(1);

        LinkedList<String> arguments = new LinkedList<String>();
        arguments.addAll(unescapeCommand(command, false));
        String label = arguments.getFirst();
        arguments.remove(0);
        String[] args = arguments.toArray(new String[0]);

        CommandEvent event = new CommandEvent(engine, sender, command, label, args);
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
                String s = escapeCommand(label);
                for (String arg : arguments) {
                    s += ' ' + escapeCommand(arg);
                }
                engine.getAppInfo().getLogger().message.println("Unknown Command: /" + s);
            }
        }
    }

    /**
     * Escapes a command
     *
     * @param str String
     * @return Escaped String
     */
    private String escapeCommand(String str) {
        return str.replace("\\", "\\\\").replace("\n", "\\n").replace("\'", "\\\'").replace("\"", "\\\"").replace("$", "\\$").replace("%", "\\%").replace(" ", "\\ ");
    }

    /**
     * Parse Escapes in a command
     *
     * @param str String
     * @return Escape translations
     */
    private Map<String, String> translateCommand(String str, boolean includeFinal) {
        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        StringBuilder part = new StringBuilder();
        int start = 0;

        boolean between = true;
        boolean literal = false;
        boolean whitespaced = false;
        for (int i = 0; i < str.length(); i++) {
            int ch = str.codePointAt(i);
            if (ch == '\'') {
                if (literal && i + 1 < str.length() && str.codePointAt(i + 1) == '\'') part.appendCodePoint(ch);
                literal = !literal;
            } else if (literal) {
                part.appendCodePoint(ch);
                between = false;
            } else {
                if (!whitespaced && ch == ' ') {
                    if (!between) {
                        map.put(str.substring(start, i), part.toString());
                        part = new StringBuilder(str.length());
                    }
                    start = i + 1;
                    between = true;
                } else {
                    between = false;
                    switch (ch) {
                        case '\"':
                            if (whitespaced && i + 1 < str.length() && str.codePointAt(i + 1) == '\"') part.appendCodePoint(ch);
                            whitespaced = !whitespaced;
                            break;
                        case '$':
                            int varEnd;
                            if ((PARSE_CONSOLE_VARIABLES.usr().equalsIgnoreCase("true") || (PARSE_CONSOLE_VARIABLES.usr().length() <= 0 && PARSE_CONSOLE_VARIABLES.get()))
                                    && i + 1 <= str.length() && (varEnd = str.indexOf('$', i+1)) > i) {
                                String var = str.substring(i + 1, varEnd);
                                String replacement;
                                if (System.getProperty(var) != null) {
                                    replacement = System.getProperty(var);
                                } else {
                                    replacement = "null";
                                }
                                part.append(replacement);
                                i = varEnd;
                            } else part.appendCodePoint(ch);
                            break;
                        case '%':
                            if ((PARSE_CONSOLE_VARIABLES.usr().equalsIgnoreCase("true") || (PARSE_CONSOLE_VARIABLES.usr().length() <= 0 && PARSE_CONSOLE_VARIABLES.get()))
                                    && i + 1 <= str.length() && (varEnd = str.indexOf('%', i+1)) > i) {
                                String var = str.substring(i + 1, varEnd);
                                String replacement;
                                if (System.getenv(var) != null) {
                                    replacement = System.getenv(var);
                                } else {
                                    replacement = "null";
                                }
                                part.append(replacement);
                                i = varEnd;
                            } else part.appendCodePoint(ch);
                            break;
                        case '\\':
                            int nextChar = (i == str.length() - 1) ? '\\' : str
                                    .codePointAt(i + 1);
                            // Octal escape?
                            if (nextChar >= '0' && nextChar <= '7') {
                                StringBuilder code = new StringBuilder();
                                code.appendCodePoint(nextChar);
                                i++;
                                if ((i < str.length() - 1) && str.codePointAt(i + 1) >= '0'
                                        && str.codePointAt(i + 1) <= '7') {
                                    code.appendCodePoint(str.codePointAt(i + 1));
                                    i++;
                                    if ((i < str.length() - 1) && str.codePointAt(i + 1) >= '0'
                                            && str.codePointAt(i + 1) <= '7') {
                                        code.appendCodePoint(str.codePointAt(i + 1));
                                        i++;
                                    }
                                }
                                part.append((char) Integer.parseInt(code.toString(), 8));
                                continue;
                            }
                            switch (nextChar) {
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
                                // Hex Unicode Char: u????
                                // Hex Unicode Codepoint: u{??????}
                                case 'u':
                                    try {
                                        if (i >= str.length() - 4) throw new IllegalStateException();
                                        StringBuilder escape = new StringBuilder();
                                        int offset = 2;

                                        if (str.codePointAt(i + 2) != '{') {
                                            if (i >= str.length() - 5) throw new IllegalStateException();
                                            while (offset <= 5) {
                                                Integer.toString(str.codePointAt(i + offset), 16);
                                                escape.appendCodePoint(str.codePointAt(i + offset));
                                                offset++;
                                            }
                                            offset--;
                                        } else {
                                            offset++;
                                            while (str.codePointAt(i + offset) != '}') {
                                                Integer.toString(str.codePointAt(i + offset), 16);
                                                escape.appendCodePoint(str.codePointAt(i + offset));
                                                offset++;
                                            }
                                        }
                                        part.append(new String(new int[]{
                                                Integer.parseInt(escape.toString(), 16)
                                        }, 0, 1));

                                        i += offset;
                                        continue;
                                    } catch (Throwable e) {
                                        ch = 'u';
                                        break;
                                    }
                            }
                            i++;
                        default:
                            part.appendCodePoint(ch);
                            break;
                    }
                }
            }
        }

        if (includeFinal || !between) map.put(str.substring(start), part.toString());
        return map;
    }

    /**
     * Unescapes a command
     *
     * @param str String
     * @return Unescaped String
     */
    private Collection<String> unescapeCommand(String str, boolean includeFinal) {
        return translateCommand(str, includeFinal).values();
    }
}
