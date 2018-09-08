package net.ME1312.Galaxi.Engine.Library;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.PluginManager;
import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.Galaxi.Plugin.Command;
import net.ME1312.Galaxi.Plugin.PluginInfo;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.charset.Charset;
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
    public static void load(GalaxiEngine engine) {
        Logger log = engine.getAppInfo().getLogger();
        
        new Command(engine.getEngineInfo()) {
            @Override
            public void command(String handle, String[] args) {
                if (args.length == 0 || engine.getPluginManager().getPlugins().get(args[0].toLowerCase()) != null) {
                    boolean patched = GalaxiEngine.class.getProtectionDomain().getCodeSource().getLocation().equals(engine.getAppInfo().get().getClass().getProtectionDomain().getCodeSource().getLocation());
                    log.message.println(
                            "These are the platforms and versions that are running " + ((args.length == 0)?engine.getAppInfo().getName():engine.getPluginManager().getPlugin(args[0]).getName()) +":",
                            "  " + System.getProperty("os.name") + ' ' + System.getProperty("os.version") + ',',
                            "  Java " + System.getProperty("java.version") + ',',
                            "  " + engine.getEngineInfo().getName() + " v" + engine.getEngineInfo().getVersion().toExtendedString() + ((engine.getEngineInfo().getSignature() != null)?" (" + engine.getEngineInfo().getSignature() + ')':"")
                                    + ((engine.getEngineInfo() == engine.getAppInfo())?" [Standalone]"+((args.length == 0)?"":","):((patched)?" [Patched],":",")));
                    if (engine.getEngineInfo() != engine.getAppInfo())
                        log.message.println("  " + engine.getAppInfo().getName() + " v" + engine.getAppInfo().getVersion().toExtendedString() + ((engine.getAppInfo().getSignature() != null)?" (" + engine.getAppInfo().getSignature() + ')':"") + ((args.length == 0)?"":","));

                    if (args.length == 0) {
                        log.message.println("");
                        if (engine.getEngineInfo() == engine.getAppInfo() || !patched) new Thread(() -> {
                            try {
                                YAMLSection tags = new YAMLSection(new JSONObject("{\"tags\":" + Util.readAll(new BufferedReader(new InputStreamReader(new URL("https://api.github.com/repos/ME1312/GalaxiEngine/git/refs/tags").openStream(), Charset.forName("UTF-8")))) + '}'));
                                List<Version> versions = new LinkedList<Version>();

                                Version updversion = engine.getEngineInfo().getVersion();
                                int updcount = 0;
                                for (YAMLSection tag : tags.getSectionList("tags")) versions.add(Version.fromString(tag.getString("ref").substring(10)));
                                Collections.sort(versions);
                                for (Version version : versions) {
                                    if (version.compareTo(updversion) > 0) {
                                        updversion = version;
                                        updcount++;
                                    }
                                }
                                if (updcount != 0) {
                                    log.message.println(engine.getEngineInfo().getName() + " v" + updversion + " is available. You are " + updcount + " version" + ((updcount == 1)?"":"s") + " behind.");
                                }
                            } catch (Exception e) {}
                        }).start();
                        try {
                            Field f = GalaxiEngine.class.getDeclaredField("updateChecker");
                            f.setAccessible(true);
                            Runnable checker = (Runnable) f.get(GalaxiEngine.getInstance());
                            f.setAccessible(false);
                            if (checker != null) checker.run();
                        } catch (Exception e) {}
                    } else {
                        PluginInfo plugin = engine.getPluginManager().getPlugin(args[0]);
                        String title = "  " + plugin.getDisplayName() + " v" + plugin.getVersion().toExtendedString() + ((engine.getEngineInfo().getSignature() != null)?" (" + plugin.getSignature() + ')':"");
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
                        log.message.println(title, subtitle);
                        if (plugin.getDescription() != null) log.message.println("", plugin.getDescription());
                    }
                } else {
                    log.message.println("There is no plugin with that name");
                }
            }
        }.usage("[plugin]").description("Gets the version of the System, Engine, and the specified Plugin").help(
                "This command will print what OS you're running, your OS version,",
                "your Java version, and the GalaxiEngine version.",
                "",
                "If the [plugin] option is provided, it will print information about the specified plugin as well.",
                "",
                "Examples:",
                "  /version",
                "  /version ExamplePlugin"
        ).register("ver", "version");
        new Command(engine.getEngineInfo()) {
            @SuppressWarnings("unchecked")
            public void command(String handle, String[] args) {
                HashMap<String, String> result = new LinkedHashMap<String, String>();
                HashMap<Command, String> reverse = new LinkedHashMap<Command, String>();
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
                    log.message.println("Command List:");
                    for (String command : result.keySet()) {
                        String formatted = result.get(command);
                        Command cmd = commands.get(command);

                        while (formatted.length() < length) {
                            formatted += ' ';
                        }
                        formatted += ((cmd.description() == null || cmd.description().length() == 0)?"  ":"- "+cmd.description());

                        log.message.println(formatted);
                    }
                } else if (commands.keySet().contains((args[0].startsWith("/"))?args[0].toLowerCase().substring(1):args[0].toLowerCase())) {
                    Command cmd = commands.get((args[0].startsWith("/"))?args[0].toLowerCase().substring(1):args[0].toLowerCase());
                    String formatted = result.get(Util.getBackwards(commands, cmd).get(0));
                    log.message.println(formatted.substring(0, formatted.length() - 1));
                    for (String line : cmd.help()) {
                        log.message.println("  " + line);
                    }
                } else {
                    log.message.println("There is no command with that name");
                }
            }
        }.usage("[command]").description("Prints a list of the commands and/or their descriptions").help(
                "This command will print a list of all currently registered commands and aliases,",
                "along with their usage and a short description.",
                "",
                "If the [command] option is provided, it will print that command, it's aliases,",
                "it's usage, and an extended description like the one you see here instead.",
                "",
                "Examples:",
                "  /help",
                "  /help version"
        ).register("help", "?");
        new Command(engine.getEngineInfo()) {
            @Override
            public void command(String handle, String[] args) {
                engine.stop();
            }
        }.description("Stops this instance of the app").help(
                "This command will shutdown this instance of the app,",
                "and any plugins currently running on it.",
                "",
                "Example:",
                "  /exit"
        ).register("exit", "end");
    }
}
