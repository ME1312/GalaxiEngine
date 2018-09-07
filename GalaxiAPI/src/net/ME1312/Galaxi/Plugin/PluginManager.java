package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Library.Event.Event;
import net.ME1312.Galaxi.Library.Event.Listener;

import java.util.Map;

public interface PluginManager {

    /**
     * Get a map of the Plugins
     *
     * @return PluginInfo Map
     */
    Map<String, PluginInfo> getPlugins();

    /**
     * Gets a Plugin
     *
     * @param name Plugin Name
     * @return PluginInfo
     */
    PluginInfo getPlugin(String name);

    /**
     * Gets a Plugin
     *
     * @param main Plugin Main Class
     * @return PluginInfo
     */
    PluginInfo getPlugin(Class<?> main);

    /**
     * Gets a Plugin
     *
     * @param main Plugin Object
     * @return PluginInfo
     */
    PluginInfo getPlugin(Object main);

    /**
     * Registers a Command
     *
     * @param command Command
     * @param handles Aliases
     */
    void addCommand(Command command, String... handles);

    /**
     * Unregisters a Command
     *
     * @param handles Aliases
     */
    void removeCommand(String... handles);

    /**
     * Register SubEvent Listeners
     *
     * @param plugin PluginInfo
     * @param listeners Listeners
     */
    void registerListener(PluginInfo plugin, Object... listeners);

    /**
     * Unregister SubEvent Listeners
     *
     * @param plugin PluginInfo
     * @param listeners Listeners
     */
    void unregisterListener(PluginInfo plugin, Object... listeners);

    /**
     * Run a SubEvent
     *
     * @param event SubEvent
     */
    void executeEvent(Event event);
}
