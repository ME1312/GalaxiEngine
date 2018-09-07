package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Engine.Library.ConsoleReader;
import net.ME1312.Galaxi.Engine.Library.DefaultCommands;
import net.ME1312.Galaxi.Engine.Library.Log.SystemLogger;
import net.ME1312.Galaxi.Event.GalaxiStartEvent;
import net.ME1312.Galaxi.Event.GalaxiStopEvent;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.UniversalFile;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.Galaxi.Plugin.Plugin;
import net.ME1312.Galaxi.Plugin.PluginInfo;
import org.fusesource.jansi.AnsiConsole;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 * Galaxi Engine Main Class
 */
@Plugin(name = "GalaxiEngine", version = "3.0.0a", authors = "ME1312", description = "An engine for command line Java applications", website = "https://github.com/ME1312/GalaxiEngine")
public class GalaxiEngine extends Galaxi {
    private final PluginManager pluginManager = new PluginManager(this);

    private final UniversalFile dir = new UniversalFile(new File(System.getProperty("user.dir")));
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
     * @param app Main class of the app (annotated with @Plugin)
     * @return The GalaxiEngine
     */
    public static GalaxiEngine init(Object app) throws Exception {
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
    private GalaxiEngine(Object app) throws Exception {
        if (app == null) app = this;

        instance = this;
        this.engine = PluginInfo.getPluginInfo(this);
        this.app = PluginInfo.getPluginInfo(app);
        if (GalaxiEngine.class.getPackage().getImplementationVersion() != null)
            engine.setSignature(new Version(GalaxiEngine.class.getPackage().getImplementationVersion()));

        pluginManager.findClasses(engine.get().getClass());
        pluginManager.findClasses(this.app.get().getClass());

        jline.console.ConsoleReader jline = new jline.console.ConsoleReader(System.in, AnsiConsole.out());
        Method m = SystemLogger.class.getDeclaredMethod("start", PrintStream.class, PrintStream.class, jline.console.ConsoleReader.class);
        m.setAccessible(true);
        m.invoke(null, AnsiConsole.out(), AnsiConsole.err(), jline);
        m.setAccessible(false);

        Field f = PluginManager.class.getDeclaredField("plugins");
        f.setAccessible(true);
        HashMap<String, PluginInfo> plugins = (HashMap<String, PluginInfo>) f.get(pluginManager);
        plugins.put(engine.getName().toLowerCase(), engine);
        plugins.put(this.app.getName().toLowerCase(), this.app);
        f.set(pluginManager, plugins);
        f.setAccessible(false);

        this.app.getLogger().info.println("Loading " + engine.getName() + " v" + engine.getVersion().toString() + " Libraries");

        console = new ConsoleReader(this, jline, running);
        DefaultCommands.load(this);
    }

    /**
     * Start the Galaxi Engine
     */
    public void start() {
        start(null);
    }

    /**
     * Start the Galaxi Engine
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
        }
    }

    /**
     * Stop the Galaxi Engine
     */
    public void stop() {
        GalaxiStopEvent event = new GalaxiStopEvent(this);
        pluginManager.executeEvent(event);
        if (!event.isCancelled() && running.get()) {
            running.set(false);

            if (onStop != null) try {
                onStop.run();
            } catch (Throwable e) {
                app.getLogger().error.println(e);
            }

            try {
                Method m = SystemLogger.class.getDeclaredMethod("stop");
                m.setAccessible(true);
                m.invoke(null);
                m.setAccessible(false);
            } catch (Exception e) {}

            System.exit(0);
        }
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
