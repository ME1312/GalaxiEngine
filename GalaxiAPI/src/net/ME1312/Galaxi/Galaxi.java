package net.ME1312.Galaxi;

import net.ME1312.Galaxi.Command.CommandProcessor;
import net.ME1312.Galaxi.Command.ConsoleCommandSender;
import net.ME1312.Galaxi.Library.UniversalFile;
import net.ME1312.Galaxi.Plugin.PluginInfo;
import net.ME1312.Galaxi.Plugin.PluginManager;

/**
 * Galaxi API Class
 */
public abstract class Galaxi {
    protected static Galaxi instance;

    /**
     * Gets the GalaxiEngine API Methods
     *
     * @return GalaxiEngine API
     */
    public static Galaxi getInstance() {
        if (instance == null) throw new IllegalStateException("Illegal call to getInstance() before engine initialization");
        return instance;
    }

    /**
     * Get the Console Sender
     *
     * @return Console Command Sender
     */
    public abstract ConsoleCommandSender getConsole();

    /**
     * Get the Command Processor
     *
     * @return Command Processor
     */
    public abstract CommandProcessor getCommandProcessor();

    /**
     * Get the Plugin Manager
     *
     * @return Plugin Manager
     */
    public abstract PluginManager getPluginManager();

    /**
     * Gets the Runtime Directory
     *
     * @return Runtime Directory
     */
    public abstract UniversalFile getRuntimeDirectory();

    /**
     * Gets the AppData Directory
     *
     * @return AppData Directory
     */
    public abstract UniversalFile getAppDataDirectory();

    /**
     * Gets the Galaxi Engine Info
     *
     * @return Galaxi Engine Info
     */
    public abstract PluginInfo getAppInfo();

    /**
     * Gets the Galaxi Engine Info
     *
     * @return Galaxi Engine Info
     */
    public abstract PluginInfo getEngineInfo();
}
