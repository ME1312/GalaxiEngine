package net.ME1312.Galaxi.Engine.Library;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.GalaxiOption;
import net.ME1312.Galaxi.Event.GalaxiReloadEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.Command.Command;
import net.ME1312.Galaxi.Plugin.Command.CommandSender;
import net.ME1312.Galaxi.Plugin.PluginInfo;
import net.ME1312.Galaxi.Plugin.PluginManager;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Default Command Class
 */
public class DefaultCommands {
    private DefaultCommands() {}

    /**
     * Load the Default Commands
     *
     * @param engine GalaxiEngine
     */
    @SuppressWarnings("unchecked")
    public static void load(GalaxiEngine engine) {
        new Command(engine.getEngineInfo()) {
            private boolean checking = false;

            @Override
            public void command(CommandSender sender, String handle, String[] args) {
                if (sender.hasPermission("galaxi.command.version")) {
                    if (args.length == 0 || engine.getPluginManager().getPlugins().get(args[0].toLowerCase()) != null) {
                        PluginInfo plugin = (args.length > 0)?engine.getPluginManager().getPlugin(args[0].toLowerCase()):engine.getAppInfo();
                        LinkedList<String> stack = new LinkedList<String>();

                        for (String item : plugin.getPlatformStack()) {
                            item = "  " + item;
                            stack.add(item);
                        }
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
                                } else {
                                    subtitle += " - ";
                                }
                                subtitle += plugin.getWebsite().toString();
                            }
                            sender.sendMessage(subtitle);
                            if (plugin.getDescription() != null) sender.sendMessage("", plugin.getDescription());
                        }

                        sender.sendMessage("");
                        if (!checking) {
                            checking = true;
                            new Thread(() -> {
                                if (engine.getEngineInfo().getUpdateChecker() != null) Util.isException(() -> engine.getEngineInfo().getUpdateChecker().run());
                                if (engine.getEngineInfo() != engine.getAppInfo() && engine.getAppInfo().getUpdateChecker() != null) Util.isException(() -> engine.getAppInfo().getUpdateChecker().run());
                                if (args.length > 0) {
                                    for (PluginInfo info : engine.getPluginManager().getPlugins().get(args[0].toLowerCase()).scanDependencies()) if (info.getUpdateChecker() != null) Util.isException(() -> info.getUpdateChecker().run());
                                    if (engine.getPluginManager().getPlugins().get(args[0].toLowerCase()).getUpdateChecker() != null) Util.isException(() -> engine.getPluginManager().getPlugins().get(args[0].toLowerCase()).getUpdateChecker().run());
                                }
                                checking = false;
                            }, Galaxi.getInstance().getEngineInfo().getName() + "::Update_Check").start();
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
                    return engine.getPluginManager().getPlugins().keySet().toArray(new String[0]);
                } else {
                    List<String> list = new ArrayList<String>();
                    for (String plugin : engine.getPluginManager().getPlugins().keySet()) {
                        if (plugin.toLowerCase().startsWith(last)) list.add(plugin);
                    }
                    return list.toArray(new String[0]);
                }
            } else {
                return new String[0];
            }
        }).usage("[plugin]").description("Gets the version of the System, Engine, App, and the specified Plugin").help(
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
        if (GalaxiOption.ENABLE_RELOAD.get()) new Command(engine.getEngineInfo()) {
            @Override
            public void command(CommandSender sender, String handle, String[] args) {
                if (sender.hasPermission("galaxi.command.reload")) {
                    sender.sendMessage("Starting reload process...");
                    long begin = Calendar.getInstance().getTime().getTime();
                    Galaxi.getInstance().getPluginManager().executeEvent(new GalaxiReloadEvent(Galaxi.getInstance()));
                    sender.sendMessage("Reload finished in " + new DecimalFormat("0.000").format((Calendar.getInstance().getTime().getTime() - begin) / 1000D) + "s");
                } else {
                    sender.sendMessage("You do not have permission to access this command");
                }
            }
        }.description("Reload the app settings").help(
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
                    TreeMap<String, Command> commands;
                    try {
                        commands = Util.reflect(PluginManager.class.getDeclaredField("commands"), engine.getPluginManager());
                    } catch (Exception e) {
                        e.printStackTrace();
                        commands = new TreeMap<String, Command>();
                    }

                    int length = 0;
                    for(String command : commands.keySet()) {
                        String formatted = "/ ";
                        Command cmd = commands.get(command);
                        String alias = (reverse.keySet().contains(cmd))?reverse.get(cmd):null;

                        if (alias != null) formatted = result.get(alias);
                        if (cmd.usage().length == 0 || alias != null) {
                            formatted = formatted.replaceFirst("\\s", ((alias != null)?"|":"") + command + ' ');
                        } else {
                            String usage = "";
                            for (String str : cmd.usage()) usage += ((usage.length() == 0)?"":" ") + str;
                            formatted = formatted.replaceFirst("\\s", command + ' ' + usage + ' ');
                        }
                        if(formatted.length() > length) {
                            length = formatted.length();
                        }

                        if (alias == null) {
                            result.put(command, formatted);
                            reverse.put(cmd, command);
                        } else {
                            result.put(alias, formatted);
                        }
                    }

                    if (args.length == 0) {
                        sender.sendMessage("Command List:");
                        for (String command : result.keySet()) {
                            String formatted = result.get(command);
                            Command cmd = commands.get(command);

                            while (formatted.length() < length) {
                                formatted += ' ';
                            }
                            formatted += ((cmd.description() == null || cmd.description().length() == 0)?"  ":"- "+cmd.description());

                            sender.sendMessage(formatted);
                        }
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
                TreeMap<String, Command> commands;
                try {
                    commands = Util.reflect(PluginManager.class.getDeclaredField("commands"), engine.getPluginManager());
                } catch (Exception e) {
                    e.printStackTrace();
                    commands = new TreeMap<String, Command>();
                }
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
        }).usage("[command]").description("Prints a list of the commands and/or their descriptions").help(
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
                    engine.stop();
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
