package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Engine.Library.ConsoleReader;
import net.ME1312.Galaxi.Engine.Library.DefaultCommands;
import net.ME1312.Galaxi.Engine.Library.Log.SystemLogger;
import net.ME1312.Galaxi.Event.GalaxiStartEvent;
import net.ME1312.Galaxi.Event.GalaxiStopEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Log.LogLevel;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.UniversalFile;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.Galaxi.Plugin.Plugin;
import net.ME1312.Galaxi.Plugin.PluginInfo;
import org.fusesource.jansi.AnsiConsole;
import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLStreamHandler;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

import static net.ME1312.Galaxi.Engine.GalaxiOption.SHOW_DEBUG_MESSAGES;

/**
 * Galaxi Engine Main Class
 */
@Plugin(name = "GalaxiEngine", version = "3.2.0b", authors = "ME1312", description = "An engine for command line Java applications", website = "https://github.com/ME1312/GalaxiEngine")
public class GalaxiEngine extends Galaxi {
    private final PluginManager pluginManager = new PluginManager(this);

    private final UniversalFile dir = new UniversalFile(GalaxiOption.APPLICATION_DIRECTORY.get());
    private final ConsoleReader console;

    private final PluginInfo app;
    private final PluginInfo engine;
    private static GalaxiEngine instance = null;

    private final Container<Boolean> running = new Container<>(false);
    private Runnable onStop = null;

    /**
     * Initialize the Galaxi Engine
     *
     * @see Plugin @Plugin
     * @param app Main class object of the app (annotated with @Plugin)
     * @return The GalaxiEngine
     */
    public static GalaxiEngine init(Object app) throws Exception {
        if (Util.isNull(app)) throw new NullPointerException();
        if (instance == null) {
            return new GalaxiEngine(PluginInfo.getPluginInfo(app));
        } else throw new IllegalStateException("Engine already initialized");
    }

    /**
     * Initialize the Galaxi Engine
     *
     * @param app PluginInfo for the app
     * @return The GalaxiEngine
     */
    public static GalaxiEngine init(PluginInfo app) throws Exception {
        if (Util.isNull(app)) throw new NullPointerException();
        if (instance == null) {
            return new GalaxiEngine(app);
        } else throw new IllegalStateException("Engine already initialized");
    }

    /**
     * Get the GalaxiEngine (null before initialization)
     *
     * @return The GalaxiEngine
     */
    public static GalaxiEngine getInstance() {
        return instance;
    }

    @SuppressWarnings("unchecked")
    private GalaxiEngine(PluginInfo app) throws Exception {
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        Util.reflect(GalaxiOption.class.getDeclaredField("lock"), null, true);

        instance = this;
        this.engine = PluginInfo.getPluginInfo(this);
        this.app = (app == null)?engine:app;

        Manifest manifest = new Manifest(GalaxiEngine.class.getResourceAsStream("/META-INF/GalaxiEngine.MF"));
        if (manifest.getMainAttributes().getValue("Implementation-Version") != null && manifest.getMainAttributes().getValue("Implementation-Version").length() > 0)
            engine.setSignature(new Version(manifest.getMainAttributes().getValue("Implementation-Version")));
        engine.setIcon(GalaxiEngine.class.getResourceAsStream("/net/ME1312/Galaxi/Engine/Library/Files/GalaxiIcon.png"));

        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        pluginManager.findClasses(engine.get().getClass());
        pluginManager.findClasses(this.app.get().getClass());

        if (!((SHOW_DEBUG_MESSAGES.usr().length() > 0 && SHOW_DEBUG_MESSAGES.def()) || (SHOW_DEBUG_MESSAGES.usr().length() <= 0 && SHOW_DEBUG_MESSAGES.get())))
            Logger.addStaticFilter((stream, message) -> (stream.getLevel() != LogLevel.DEBUG)?null:false);
        jline.console.ConsoleReader jline = new jline.console.ConsoleReader(System.in, AnsiConsole.out);
        Util.reflect(SystemLogger.class.getDeclaredMethod("start", PrintStream.class, PrintStream.class, jline.console.ConsoleReader.class), null, AnsiConsole.out(), AnsiConsole.err(), jline);

        this.app.getLogger().info.println("Loading " + engine.getName() + " v" + engine.getVersion().toString() + " Libraries");
        for (PluginInfo.Dependency depend : this.app.getDependancies()) {
            if (engine.getName().equalsIgnoreCase(depend.getName())) {
                if (depend.getMinVersion() == null || engine.getVersion().compareTo(depend.getMinVersion()) >= 0) {
                    if (!(depend.getMaxVersion() == null || engine.getVersion().compareTo(depend.getMaxVersion()) < 0)) {
                        throw new IllegalStateException("Engine version is too new for this app: " + depend.getName() + " v" + engine.getVersion().toString() + " (should be below " + depend.getMaxVersion() + ")");
                    }
                } else {
                    throw new IllegalStateException("Engine version is too old for this app: " + depend.getName() + " v" + engine.getVersion().toString() + " (should be at or above " + depend.getMinVersion() + ")");
                }
            }
        }
        if (app == null) this.app.getLogger().warn.println("GalaxiEngine is running in standalone mode");
        else if (engine.getName().equalsIgnoreCase(this.app.getName())) throw new IllegalStateException("App name cannot be the same as the Engine's name");

        console = new ConsoleReader(this, jline, running);
        DefaultCommands.load(this);

        URL.setURLStreamHandlerFactory(protocol -> {
            HashMap<String, URLStreamHandler> protocols;
            try {
                protocols = Util.reflect(Galaxi.class.getDeclaredField("protocols"), this);
            } catch (Exception e) {
                e.printStackTrace();
                protocols = new HashMap<String, URLStreamHandler>();
            }

            if (protocols.keySet().contains(protocol.toLowerCase())) {
                return protocols.get(protocol.toLowerCase());
            } else {
                return null;
            }
        });

        engine.setUpdateChecker(() -> {
            if (engine == this.app || !GalaxiEngine.class.getProtectionDomain().getCodeSource().getLocation().equals(this.app.get().getClass().getProtectionDomain().getCodeSource().getLocation())) try {
                YAMLSection tags = new YAMLSection(new JSONObject("{\"tags\":" + Util.readAll(new BufferedReader(new InputStreamReader(new URL("https://api.github.com/repos/ME1312/GalaxiEngine/git/refs/tags").openStream(), Charset.forName("UTF-8")))) + '}'));
                List<Version> versions = new LinkedList<Version>();

                Version updversion = getEngineInfo().getVersion();
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
                    getAppInfo().getLogger().message.println(engine.getName() + " v" + updversion + " is available. You are " + updcount + " version" + ((updcount == 1)?"":"s") + " behind.");
                }
            } catch (Exception e) {}
        });
    }

    /**
     * Start the GalaxiEngine
     */
    public void start() {
        start(null);
    }

    /**
     * Start the GalaxiEngine
     *
     * @param callback Callback for when Galaxi is stopped
     */
    public void start(Runnable callback) {
        if (!running.get()) {
            try {
                onStop = callback;
                running.set(true);
                console.start();
                pluginManager.executeEvent(new GalaxiStartEvent(this));
            } catch (Exception e) {}

            new Timer(getEngineInfo().getName() + "::Routine_Update_Check").schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        if (engine.getUpdateChecker() != null) engine.getUpdateChecker().run();
                        if (engine != app && app.getUpdateChecker() != null) app.getUpdateChecker().run();
                    } catch (Exception e) {}
                }
            }, 0, TimeUnit.DAYS.toMillis(2));
        }
    }

    /**
     * Stop the GalaxiEngine
     */
    public void stop() {
        stop(0);
    }

    /**
     * Stop the GalaxiEngine
     *
     * @param code Exit Code
     */
    public void stop(int code) {
        if (!stopping) {
            stopping = true;
            GalaxiStopEvent event = new GalaxiStopEvent(this, code);
            pluginManager.executeEvent(event);
            if (!event.isCancelled()) {
                exit(code);
            } else stopping = false;
        }
    }

    /**
     * Force stop the GalaxiEngine
     *
     * @param code Exit Code
     */
    public void terminate(int code) {
        stopping = true;
        GalaxiStopEvent event = new GalaxiStopEvent(this, code);
        pluginManager.executeEvent(event);
        exit(code);
    }

    private boolean stopping = false;
    private void exit(int code) {
        running.set(false);

        if (onStop != null) try {
            onStop.run();
        } catch (Throwable e) {
            app.getLogger().error.println(e);
        }

        Util.isException(() -> Util.reflect(SystemLogger.class.getDeclaredMethod("stop"), null));

        System.exit(code);
    }

    /**
     * Get the ConsoleReader
     *
     * @return ConsoleReader
     */
    public ConsoleReader getConsoleReader() {
        return console;
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public UniversalFile getRuntimeDirectory() {
        return dir;
    }

    @Override
    public PluginInfo getAppInfo() {
        return app;
    }

    @Override
    public PluginInfo getEngineInfo() {
        return engine;
    }
}
