package net.ME1312.Galaxi.Plugin;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;

import java.util.UUID;

/**
 * Task Builder Class
 */
public abstract class TaskBuilder implements Runnable {
    private long repeat = -1L;
    private long delay = -1L;
    private PluginInfo plugin;

    /**
     * Create a new Task
     *
     * @param plugin Plugin Creating
     */
    public TaskBuilder(PluginInfo plugin) {
        if (Util.isNull(plugin)) throw new NullPointerException();
        this.plugin = plugin;
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
     * Set the Repeat Interval for this task
     *
     * @param value Value
     * @return Task Builder
     */
    public TaskBuilder repeat(long value) {
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
    public TaskBuilder delay(long value) {
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
     * @return Task ID
     */
    public UUID schedule() {
        return Galaxi.getInstance().schedule(this);
    }
}
