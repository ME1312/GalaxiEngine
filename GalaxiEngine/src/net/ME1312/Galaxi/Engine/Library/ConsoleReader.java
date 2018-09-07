package net.ME1312.Galaxi.Engine.Library;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.PluginManager;
import net.ME1312.Galaxi.Event.ConsoleChatEvent;
import net.ME1312.Galaxi.Event.ConsoleCommandEvent;
import net.ME1312.Galaxi.Library.Callback;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.Command;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Console Reader Class
 */
public class ConsoleReader extends Thread {
    private Container<Boolean> running;
    private jline.console.ConsoleReader jline;
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
    }

    /**
     * Start the ConsoleReader loop
     */
    public void run() {
        try {
            String line;
            while (running.get() && (line = jline.readLine(">")) != null) {
                if (!running.get() || line.replaceAll("\\s", "").length() == 0) continue;
                if (line.startsWith("/") && chat != null) {
                    try {
                        ConsoleChatEvent event = new ConsoleChatEvent(engine, Util.unescapeJavaString(line));
                        engine.getPluginManager().executeEvent(event);
                        if (!event.isCancelled()) chat.run(event.getMessage());
                    } catch (Exception e) {
                        engine.getAppInfo().getLogger().error.print(e);
                    }
                } else {
                    runCommand(line);
                }
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
     * Run a command
     *
     * @param command Command
     */
    @SuppressWarnings("unchecked")
    public void runCommand(String command) {
        ConsoleCommandEvent event = new ConsoleCommandEvent(engine, command);
        engine.getPluginManager().executeEvent(event);
        if (!event.isCancelled()) {
            String line = event.getCommand();
            LinkedList<String> args = new LinkedList<String>();
            Matcher parser = Pattern.compile("(?:^|\\s+)(\"(?:\\\\\"|[^\"])+\"?|(?:\\\\\\s|[^\\s])+)").matcher(line);
            while (parser.find()) {
                String arg = parser.group(1);
                if (arg.startsWith("\"")) arg = arg.substring(1, arg.length() - ((arg.endsWith("\"")) ? 1 : 0));
                arg = parseCommand(arg);
                args.add(arg);
            }
            String cmd = args.get(0);
            args.remove(0);
            if (cmd.startsWith("/")) cmd = cmd.substring(1);

            TreeMap<String, Command> commands;
            try {
                Field f = PluginManager.class.getDeclaredField("commands");
                f.setAccessible(true);
                commands = (TreeMap<String, Command>) f.get(engine.getPluginManager());
                f.setAccessible(false);
            } catch (Exception e) {
                e.printStackTrace();
                commands = new TreeMap<String, Command>();
            }

            if (commands.keySet().contains(cmd.toLowerCase())) {
                try {
                    commands.get(cmd.toLowerCase()).command(cmd, args.toArray(new String[args.size()]));
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
            char ch = str.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == str.length() - 1) ? '\\' : str
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < str.length() - 1) && str.charAt(i + 1) >= '0'
                            && str.charAt(i + 1) <= '7') {
                        code += str.charAt(i + 1);
                        i++;
                        if ((i < str.length() - 1) && str.charAt(i + 1) >= '0'
                                && str.charAt(i + 1) <= '7') {
                            code += str.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
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
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= str.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + str.charAt(i + 2) + str.charAt(i + 3)
                                        + str.charAt(i + 4) + str.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }
}
