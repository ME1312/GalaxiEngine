package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;

import java.util.UUID;

/**
 * Task Builder Class
 */
public abstract class Task implements Runnable {
    private long repeat = -1L;
    private long delay = -1L;
    private PluginInfo plugin;
    private String name;
    private String identifier;

    /**
     * Create a new Task
     *
     * @param plugin Plugin Creating
     */
    public Task(PluginInfo plugin) {
        this(plugin, null);
    }

    /**
     * Create a new Task
     *
     * @param plugin Plugin Creating
     * @param name Task Name
     */
    public Task(PluginInfo plugin, String name) {
        this(plugin, name, null);
    }

    /**
     * Create a new Task
     *
     * @param plugin Plugin Creating
     * @param name Task Name
     * @param identifier Task Identifier
     */
    public Task(PluginInfo plugin, String name, String identifier) {
        if (Util.isNull(plugin)) throw new NullPointerException();
        this.plugin = plugin;
        this.name = name;
        this.identifier = identifier;
    }

    /**
     * Get the Plugin that created this task
     *
     * @return Plugin Info
     */
    public PluginInfo plugin() {
        return this.plugin;
    }

    /**
     * Get the name of the Task
     *
     * @return Task Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the identifier of the Task
     *
     * @return Task Identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Set the Repeat Interval for this task
     *
     * @param value Value
     * @return Task Builder
     */
    public Task repeat(long value) {
        if (Util.isNull(value)) throw new NullPointerException();
        this.repeat = value;
        return this;
    }

    /**
     * Get the Repeat Interval for this task
     *
     * @return Repeat Interval
     */
    public long repeat() {
        return this.repeat;
    }

    /**
     * Delay this task
     *
     * @param value Value
     * @return Task Builder
     */
    public Task delay(long value) {
        if (Util.isNull(value)) throw new NullPointerException();
        this.delay = value;
        return this;
    }

    /**
     * Get the Delay for this task
     *
     * @return Task Delay
     */
    public long delay() {
        return this.delay;
    }

    /**
     * Schedule this task
     *
     * @return Unique Task ID
     */
    public UUID schedule() {
        return Galaxi.getInstance().schedule(this);
    }
}
