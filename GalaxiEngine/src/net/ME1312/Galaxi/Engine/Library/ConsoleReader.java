package net.ME1312.Galaxi.Engine.Library;

import jline.console.completer.Completer;
import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Event.ConsoleChatEvent;
import net.ME1312.Galaxi.Event.ConsoleCommandEvent;
import net.ME1312.Galaxi.Library.Callback;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.Command.Command;
import net.ME1312.Galaxi.Plugin.Command.CommandSender;
import net.ME1312.Galaxi.Plugin.Command.CompletionHandler;
import net.ME1312.Galaxi.Plugin.Command.ConsoleCommandSender;
import net.ME1312.Galaxi.Plugin.PluginManager;

import java.awt.*;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.ME1312.Galaxi.Engine.GalaxiOption.SHOW_CONSOLE_WINDOW;

/**
 * Console Reader Class
 */
public class ConsoleReader extends Thread implements Completer {
    private Container<Boolean> running;
    private jline.console.ConsoleReader jline;
    private OutputStream window;
    private Callback<String> chat = null;
    private GalaxiEngine engine;

    /**
     * Create a ConsoleReader
     *
     * @param engine GalaxiEngine
     * @param jline JLine Reader
     * @param status Status Container
     */
    public ConsoleReader(GalaxiEngine engine, jline.console.ConsoleReader jline, Container<Boolean> status) {
        this.engine = engine;
        this.jline = jline;
        this.running = status;
        try {
            if (SHOW_CONSOLE_WINDOW.usr().equalsIgnoreCase("true") || SHOW_CONSOLE_WINDOW.get() && System.console() == null) {
                openConsoleWindow(true);
            }
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.println(e);
        }

        jline.addCompleter(this);
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

    @SuppressWarnings("unchecked")
    @Override
    public int complete(String full, int cursor, List<CharSequence> candidates) {
        if (full != null && full.length() > 0 && (chat == null || full.startsWith("/"))) {
            String before = "";
            String last = null;
            LinkedList<String> args = new LinkedList<String>();
            Matcher parser = Pattern.compile("(?:^|\\s+)(\"(?:\\\\\"|[^\"])+\"?|(?:\\\\\\s|[^\\s])+)?").matcher(full);
            while (parser.find()) {
                if (last != null) before += last;
                String arg = parser.group(1);
                if (arg != null) {
                    if (arg.startsWith("\"")) arg = arg.substring(1, arg.length() - ((arg.length() > 1 && arg.endsWith("\""))?1:0));
                    arg = parseCommand(arg);
                } else arg = "";
                args.add(arg);
                last = parser.group();
            }
            String cmd = args.get(0);
            args.remove(0);
            if (cmd.startsWith("/")) cmd = cmd.substring(1);

            TreeMap<String, Command> commands;
            try {
                commands = Util.reflect(PluginManager.class.getDeclaredField("commands"), engine.getPluginManager());
            } catch (Exception e) {
                e.printStackTrace();
                commands = new TreeMap<String, Command>();
            }

            if (args.size() == 0) {
                if (cmd.length() > 0)
                    for (String handle : commands.keySet())
                        if (handle.startsWith(cmd.toLowerCase()))
                            candidates.add(((full.startsWith("/"))?"/":"") + handle.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace(" ", "\\ "));
            } else if (commands.keySet().contains(cmd.toLowerCase())) {
                CompletionHandler autocompletor = commands.get(cmd.toLowerCase()).autocomplete();
                if (autocompletor != null)
                    for (String autocomplete : autocompletor.complete(ConsoleCommandSender.get(), cmd, args.toArray(new String[args.size()])))
                        if (!Util.isNull(autocomplete) && autocomplete.length() > 0)
                            candidates.add(before + ' ' + autocomplete.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace(" ", "\\ "));
            }
        }
        return candidates.isEmpty()?-1:0;
    }

    /**
     * Start the ConsoleReader loop
     */
    public void run() {
        try {
            String line;
            while (running.get() && (line = jline.readLine(">")) != null) {
                if (!running.get() || line.replaceAll("\\s", "").length() == 0) continue;
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
        } catch (Exception e) {
            engine.getAppInfo().getLogger().error.println(e);
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
        ConsoleCommandEvent event = new ConsoleCommandEvent(engine, command);
        engine.getPluginManager().executeEvent(event);
        if (!event.isCancelled()) {
            String line = event.getCommand();
            LinkedList<String> args = new LinkedList<String>();
            Matcher parser = Pattern.compile("(?:^|\\s+)(\"(?:\\\\\"|[^\"])+\"?|(?:\\\\\\s|[^\\s])+)").matcher(line);
            while (parser.find()) {
                String arg = parser.group(1);
                if (arg.startsWith("\"")) arg = arg.substring(1, arg.length() - ((arg.length() > 1 && arg.endsWith("\""))?1:0));
                arg = parseCommand(arg);
                args.add(arg);
            }
            String cmd = args.get(0);
            args.remove(0);
            if (cmd.startsWith("/")) cmd = cmd.substring(1);

            TreeMap<String, Command> commands;
            try {
                commands = Util.reflect(PluginManager.class.getDeclaredField("commands"), engine.getPluginManager());
            } catch (Exception e) {
                e.printStackTrace();
                commands = new TreeMap<String, Command>();
            }

            if (commands.keySet().contains(cmd.toLowerCase())) {
                try {
                    commands.get(cmd.toLowerCase()).command(sender, cmd, args.toArray(new String[args.size()]));
                } catch (Exception e) {
                    engine.getAppInfo().getLogger().error.println(new InvocationTargetException(e, "Uncaught exception while running command"));
                }
            } else {
                String s = cmd.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace(" ", "\\ ");
                for (String arg : args) {
                    s += ' ' + arg.replace("\\", "\\\\").replace("\n", "\\n").replace("\"", "\\\"").replace(" ", "\\ ");
                }
                engine.getAppInfo().getLogger().message.println("Unknown Command - " + s);
            }

            try {
                jline.getOutput().write("\b \b");
            } catch (Exception e) {
                engine.getAppInfo().getLogger().error.print(e);
            }
        }
    }

    /**
     * Parse escapes in a command
     *
     * @param str String
     * @return Unescaped String
     */
    private String parseCommand(String str) {
        StringBuilder sb = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            int ch = str.codePointAt(i);
            if (ch == '\\') {
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
                    sb.append((char) Integer.parseInt(code.toString(), 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case ' ':
                        ch = ' ';
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
                            sb.append(new String(new int[]{
                                    Integer.parseInt(escape.toString(), 16)
                            }, 0, 1));

                            i += offset;
                            continue;
                        } catch (Throwable e){
                            ch = 'u';
                            break;
                        }
                }
                i++;
            }
            sb.appendCodePoint(ch);
        }
        return sb.toString();
    }
}
