package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Command.Command;
import net.ME1312.Galaxi.Command.CommandSender;
import net.ME1312.Galaxi.Command.ConsoleCommandSender;
import net.ME1312.Galaxi.Engine.GalaxiOption;
import net.ME1312.Galaxi.Event.Engine.GalaxiReloadEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Callback.ReturnRunnable;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Log.ConsoleTextElement;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

/**
 * Default Command Class
 */
class Commands {
    private Commands() {}

    /**
     * Load the Default Commands
     *
     * @param engine GalaxiEngine
     */
    @SuppressWarnings("unchecked")
    static void load(Engine engine) {
        new Command(engine.getEngineInfo()) {
            private boolean checking = false;

            @Override
            public void command(CommandSender sender, String handle, String[] args) {
                if (sender.hasPermission("galaxi.command.version")) {
                    if (args.length == 0 || engine.code.getPlugins().get(args[0].toLowerCase()) != null) {
                        PluginInfo plugin = (args.length > 0)?engine.code.getPlugin(args[0].toLowerCase()):engine.getAppInfo();
                        LinkedList<String> stack = new LinkedList<String>();

                        for (String item : plugin.getPlatformStack()) {
                            item = "  " + item;
                            stack.add(item);
                        }
                        sender.sendMessage("These are the platforms and versions that are running " + ((args.length == 0)?engine.getAppInfo().getName():engine.code.getPlugin(args[0]).getName()) +":");
                        sender.sendMessage(stack.toArray(new String[0]));

                        if (args.length > 0) {
                            String title = stack.get(stack.size() - 1);
                            String subtitle = "    by ";
                            int i = 0;
                            for (String author : plugin.getAuthors()) {
                                i++;
                                if (i > 1) {
                                    if (plugin.getAuthors().size() > 2) subtitle += ", ";
                                    else if (plugin.getAuthors().size() == 2) subtitle += ' ';
                                    if (i == plugin.getAuthors().size()) subtitle += "and ";
                                }
                                subtitle += author;
                            }
                            if (plugin.getWebsite() != null) {
                                if (title.length() > subtitle.length() + 5 + plugin.getWebsite().toString().length()) {
                                    i = subtitle.length();
                                    while (i < title.length() - plugin.getWebsite().toString().length() - 2) {
                                        i++;
                                        subtitle += ' ';
                                    }
                                } else if (Math.max(title.length(), 37) <= subtitle.length()) {
                                    subtitle += "\n    ";
                                } else {
                                    subtitle += " - ";
                                }
                                subtitle += plugin.getWebsite().toString();
                            }
                            sender.sendMessage(subtitle);
                            if (plugin.getDescription() != null) sender.sendMessage("", plugin.getDescription());
                        }

                        if (!checking) {
                            checking = true;
                            LinkedList<ReturnRunnable<Boolean>> checks = new LinkedList<>();

                            if (engine.getEngineInfo().getUpdateChecker() != null) checks.add(engine.getEngineInfo().getUpdateChecker());
                            if (engine.getEngineInfo() != engine.getAppInfo() && engine.getAppInfo().getUpdateChecker() != null) checks.add(engine.getAppInfo().getUpdateChecker());
                            if (args.length > 0) {
                                for (PluginInfo info : plugin.scanDependencies()) if (info.getUpdateChecker() != null) checks.add(info.getUpdateChecker());
                                if (plugin.getUpdateChecker() != null) checks.add(plugin.getUpdateChecker());
                            }

                            if (checks.size() != 0) {
                                sender.sendMessage("");
                                new Thread(() -> {
                                    boolean updated = true;
                                    for (ReturnRunnable<Boolean> check : checks) try {
                                        updated = check.run() != Boolean.TRUE && updated;
                                    } catch (Throwable e) {
                                        engine.getAppInfo().getLogger().error.println(new InvocationTargetException(e, "Unhandled exception while checking version"));
                                    }
                                    if (updated) sender.sendMessage("You are on the latest version.");
                                    checking = false;
                                }, Galaxi.getInstance().getEngineInfo().getName() + "::Update_Check").start();
                            } else checking = false;
                        }
                    } else {
                        sender.sendMessage("There is no plugin with that name");
                    }
                } else {
                    sender.sendMessage("You do not have permission to access this command");
                }
            }
        }.autocomplete((sender, handle, args) -> {
            if (args.length <= 1) {
                String last = (args.length > 0)?args[args.length - 1].toLowerCase():"";
                if (last.length() == 0) {
                    return engine.code.getPlugins().keySet().toArray(new String[0]);
                } else {
                    List<String> list = new ArrayList<String>();
                    for (String plugin : engine.code.getPlugins().keySet()) {
                        if (plugin.toLowerCase().startsWith(last)) list.add(plugin);
                    }
                    return list.toArray(new String[0]);
                }
            } else {
                return new String[0];
            }
        }).usage("[plugin]").description("Prints versioning information").help(
                "This command will print what OS you're running, your OS version,",
                "your Java version, the GalaxiEngine version, and the app version.",
                "",
                "If the [plugin] option is provided, it will additionally print information about",
                "the specified plugin and it's dependencies.",
                "",
                "Permission: galaxi.command.version",
                "Examples:",
                "  /version",
                "  /version ExamplePlugin"
        ).register("ver", "version");
        if (GalaxiOption.ENABLE_RELOAD.app()) new Command(engine.getEngineInfo()) {
            @Override
            public void command(CommandSender sender, String handle, String[] args) {
                if (sender.hasPermission("galaxi.command.reload")) {
                    sender.sendMessage("Starting reload process...");
                    long begin = Calendar.getInstance().getTime().getTime();
                    engine.code.executeEvent(new GalaxiReloadEvent(Galaxi.getInstance()));
                    sender.sendMessage("Reload finished in " + new DecimalFormat("0.000").format((Calendar.getInstance().getTime().getTime() - begin) / 1000D) + "s");
                } else {
                    sender.sendMessage("You do not have permission to access this command");
                }
            }
        }.description("Reloads app settings").help(
                "This command will reload the configuration for the app",
                "and any plugins that opt-in via the reload event.",
                "",
                "",
                "Permission: galaxi.command.reload",
                "Examples:",
                "  /reload"
        ).register("reload");
        new Command(engine.getEngineInfo()) {
            @SuppressWarnings("unchecked")
            public void command(CommandSender sender, String handle, String[] args) {
                if (sender.hasPermission("galaxi.command.help")) {
                    HashMap<String, String> result = new LinkedHashMap<String, String>();
                    HashMap<Command, String> reverse = new LinkedHashMap<Command, String>();
                    TreeMap<String, Command> commands = engine.code.commands;

                    int label = 0, description = 0;
                    for(String command : commands.keySet()) {
                        String formatted = "/ ";
                        Command cmd = commands.get(command);
                        String alias = reverse.getOrDefault(cmd, null);

                        if (alias != null) formatted = result.get(alias);
                        if (cmd.usage().length == 0 || alias != null) {
                            formatted = formatted.replaceFirst("\\s", ((alias != null)?"|":"") + command + ' ');
                        } else {
                            String usage = "";
                            for (String str : cmd.usage()) usage += ((usage.length() == 0)?"":" ") + str;
                            formatted = formatted.replaceFirst("\\s", command + ' ' + usage + ' ');
                        }

                        if (formatted.length() > label) label = formatted.length();
                        if (cmd.description() != null && cmd.description().length() > description) description = cmd.description().length();

                        if (alias == null) {
                            result.put(command, formatted);
                            reverse.put(cmd, command);
                        } else {
                            result.put(alias, formatted);
                        }
                    }

                    ++label;
                    description += 3;
                    if (args.length == 0) {
                        boolean color = false;
                        int a = 236, b = 237;
                        StringBuilder formatted = new StringBuilder("Command Listing");
                        StringBuilder blank = new StringBuilder("  ");
                        if (sender instanceof ConsoleCommandSender) for (int limit = label + description + 3, i = blank.length(); i < limit; ++i) {
                            if (i == formatted.length()) formatted.append(' ');
                            blank.append(' ');
                        }

                        formatted.append('\n');
                        blank.append('\n');
                        sender.sendMessage(new ConsoleTextElement(formatted.toString()).backgroundColor(b));
                        for (Iterator<String> set = result.keySet().iterator(); set.hasNext();) {
                            String command = set.next(), text = result.get(command);
                            Command cmd = commands.get(command);
                            formatted = new StringBuilder();
                            color = !color;

                            formatted.append("   ");
                            formatted.append(text);
                            if (sender instanceof ConsoleCommandSender || cmd.description() != null) {
                                for (int i = text.length(); i < label; ++i) {
                                    formatted.append(' ');
                                }

                                if (cmd.description() != null) formatted.append(cmd.description());
                                if (sender instanceof ConsoleCommandSender) for (int i = (cmd.description() == null)? 0 : cmd.description().length(); i < description; ++i) {
                                    formatted.append(' ');
                                }
                            }

                            formatted.append('\n');
                            sender.sendMessage(new ConsoleTextElement(formatted.toString()).backgroundColor((color)?a:b));
                        }
                        sender.sendMessage(new ConsoleTextElement(blank.toString()).backgroundColor((!color)?a:b));
                    } else if (commands.keySet().contains((args[0].startsWith("/"))?args[0].toLowerCase().substring(1):args[0].toLowerCase())) {
                        Command cmd = commands.get((args[0].startsWith("/"))?args[0].toLowerCase().substring(1):args[0].toLowerCase());
                        String formatted = result.get(Util.getBackwards(commands, cmd).get(0));
                        sender.sendMessage(formatted.substring(0, formatted.length() - 1));
                        for (String line : cmd.help()) {
                            sender.sendMessage("  " + line);
                        }
                    } else {
                        sender.sendMessage("There is no command with that name");
                    }
                } else {
                    sender.sendMessage("You do not have permission to access this command");
                }
            }
        }.autocomplete((sender, handle, args) -> {
            if (args.length <= 1) {
                String last = (args.length > 0)?args[args.length - 1].toLowerCase():"";
                TreeMap<String, Command> commands = engine.code.commands;

                if (last.length() == 0) {
                    return commands.keySet().toArray(new String[0]);
                } else {
                    List<String> list = new ArrayList<String>();
                    for (String command : commands.keySet()) {
                        if (command.toLowerCase().startsWith(last)) list.add(command);
                    }
                    return list.toArray(new String[0]);
                }
            } else {
                return new String[0];
            }
        }).usage("[command]").description("Prints help on using commands").help(
                "This command will print a list of all currently registered commands and aliases,",
                "along with their usage and a short description.",
                "",
                "If the [command] option is provided, it will print that command, it's aliases,",
                "it's usage, and an extended description like the one you see here instead.",
                "",
                "Permission: galaxi.command.help",
                "Examples:",
                "  /help",
                "  /help version"
        ).register("help", "?");
        new Command(engine.getEngineInfo()) {
            @Override
            public void command(CommandSender sender, String handle, String[] args) {
                if (sender.hasPermission("galaxi.command.exit")) {
                    new Thread(engine::stop, Galaxi.getInstance().getEngineInfo().getName() + "::CMD_Shutdown").start();
                } else {
                    sender.sendMessage("You do not have permission to access this command");
                }
            }
        }.description("Stops this instance of the app").help(
                "This command will shutdown this instance of the app,",
                "and any plugins currently running on it.",
                "",
                "Permission: galaxi.command.exit",
                "Example:",
                "  /exit"
        ).register("exit", "end");
    }
}
