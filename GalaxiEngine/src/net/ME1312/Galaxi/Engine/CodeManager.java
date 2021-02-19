package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Plugin.PluginManager;

import java.io.File;
import java.io.IOException;

/**
 * GalaxiEngine Code Manager Class
 */
public abstract class CodeManager extends PluginManager {


    /**
     * Catalog classes for a library
     *
     * @param clazz A class from the library to search
     */
    public abstract String[] catalogLibrary(Class<?> clazz) throws IOException;

    /**
     * Load plugins
     *
     * @param directories Directories to search
     * @return Amount of plugins loaded
     */
    public abstract int loadPlugins(File... directories);
}
