package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Command.ConsoleCommandSender;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.App;
import net.ME1312.Galaxi.Plugin.Plugin;
import net.ME1312.Galaxi.Plugin.PluginInfo;

/**
 * GalaxiEngine Main Class
 */
public abstract class GalaxiEngine extends Galaxi {
    private ConsoleCommandSender console = new ConsoleCommandSender() {};

    /**
     * Initialize the Galaxi Engine
     *
     * @see App @App
     * @see Plugin @Plugin
     * @param app Main class object of the app (annotated with @App/@Plugin)
     * @return The GalaxiEngine
     */
    public static GalaxiEngine init(Object app) throws Exception {
        if (Util.isNull(app)) throw new NullPointerException();
        if (instance == null) {
            return Util.reflect(Class.forName("net.ME1312.Galaxi.Engine.Runtime.Engine").getDeclaredConstructor(PluginInfo.class), PluginInfo.load(app));
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
            return Util.reflect(Class.forName("net.ME1312.Galaxi.Engine.Runtime.Engine").getDeclaredConstructor(PluginInfo.class), app);
        } else throw new IllegalStateException("Engine already initialized");
    }

    /**
     * Get the GalaxiEngine
     *
     * @return The GalaxiEngine
     */
    public static GalaxiEngine getInstance() {
        return (GalaxiEngine) Galaxi.getInstance();
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
    public abstract void start(Runnable callback);

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
    public abstract void stop(int code);

    /**
     * Force stop the GalaxiEngine
     *
     * @param code Exit Code
     */
    public abstract void terminate(int code);

    /**
     * Override the Console Sender
     *
     * @param sender Console Command Sender
     */
    public void setConsole(ConsoleCommandSender sender) {
        console = sender;
    }

    @Override
    public ConsoleCommandSender getConsole() {
        return console;
    }

    @Override
    public abstract CommandParser getCommandProcessor();

    @Override
    public abstract CodeManager getPluginManager();
}
