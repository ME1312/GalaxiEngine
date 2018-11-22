package net.ME1312.Galaxi;

import net.ME1312.Galaxi.Library.UniversalFile;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Plugin.PluginInfo;
import net.ME1312.Galaxi.Plugin.PluginManager;
import net.ME1312.Galaxi.Plugin.TaskBuilder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class Galaxi {
    private HashMap<UUID, Timer> schedule = new HashMap<UUID, Timer>();
    private HashMap<String, URLStreamHandler> protocols = new HashMap<String, URLStreamHandler>();

    /**
     * Gets the GalaxiEngine API Methods
     *
     * @return GalaxiEngine API
     */
    public static Galaxi getInstance() {
        return (Galaxi) Util.getDespiteException(() -> Class.forName("net.ME1312.Galaxi.Engine.GalaxiEngine").getDeclaredMethod("getInstance").invoke(null), null);
    }

    /**
     * Get the Plugin Manager
     *
     * @return Plugin Manager
     */
    public abstract PluginManager getPluginManager();

    /**
     * Schedule a task
     *
     * @param builder SubTaskBuilder
     * @return Task ID
     */
    public UUID schedule(TaskBuilder builder) {
        if (Util.isNull(builder)) throw new NullPointerException();
        UUID sid = Util.getNew(schedule.keySet(), UUID::randomUUID);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    builder.run();
                } catch (Throwable e) {
                    builder.plugin().getLogger().error.println(new InvocationTargetException(e, "Unhandled exception while running Task " + sid.toString()));
                }
                if (builder.repeat() <= 0) schedule.remove(sid);
            }
        };

        schedule.put(sid, new Timer("ScheduledTask_" + sid.toString()));
        if (builder.repeat() > 0) {
            if (builder.delay() > 0) {
                schedule.get(sid).scheduleAtFixedRate(task, builder.delay(), builder.repeat());
            } else {
                schedule.get(sid).scheduleAtFixedRate(task, new Date(), builder.repeat());
            }
        } else {
            if (builder.delay() > 0) {
                schedule.get(sid).schedule(task, builder.delay());
            } else {
                new Thread(task).start();
            }
        }
        return sid;
    }

    /**
     * Schedule a task
     *
     * @param plugin Plugin Scheduling
     * @param run What to run
     * @return Task ID
     */
    public UUID schedule(PluginInfo plugin, Runnable run) {
        return schedule(plugin, run, -1L);
    }

    /**
     * Schedule a task
     *
     * @param plugin Plugin Scheduling
     * @param run What to Run
     * @param delay Task Delay
     * @return Task ID
     */
    public UUID schedule(PluginInfo plugin, Runnable run, long delay) {
        return schedule(plugin, run, delay, -1L);
    }

    /**
     * Schedule a task
     *
     * @param plugin Plugin Scheduling
     * @param run What to Run
     * @param delay Task Delay
     * @param repeat Task Repeat Interval
     * @return Task ID
     */
    public UUID schedule(PluginInfo plugin, Runnable run, long delay, long repeat) {
        return schedule(plugin, run, TimeUnit.MILLISECONDS, delay, repeat);
    }

    /**
     * Schedule a task
     *
     * @param plugin Plugin Scheduling
     * @param run What to Run
     * @param unit TimeUnit to use
     * @param delay Task Delay
     * @param repeat Task Repeat Interval
     * @return Task ID
     */
    public UUID schedule(PluginInfo plugin, Runnable run, TimeUnit unit, long delay, long repeat) {
        if (Util.isNull(plugin, run, unit, delay, repeat)) throw new NullPointerException();
        return schedule(new TaskBuilder(plugin) {
            @Override
            public void run() {
                run.run();
            }
        }.delay(unit.toMillis(delay)).repeat(unit.toMillis(repeat)));
    }

    /**
     * Cancel a task
     *
     * @param sid Task ID
     */
    public void cancelTask(UUID sid) {
        if (Util.isNull(sid)) throw new NullPointerException();
        if (schedule.keySet().contains(sid)) {
            schedule.get(sid).cancel();
            schedule.remove(sid);
        }
    }

    /**
     * Registers a Protocol
     *
     * @param generator Stream Generator
     * @param handles Protocol Handles
     */
    public void addProtocol(URLStreamHandler generator, String... handles) {
        for (String handle : handles) {
            protocols.put(handle.toLowerCase(), generator);
        }
    }

    /**
     * Unregisters a Protocol
     *
     * @param handles Protocol Handles
     */
    public void removeProtocol(String... handles) {
        for (String handle : handles) {
            protocols.remove(handle.toLowerCase());
        }
    }

    /**
     * Gets the Runtime Directory
     *
     * @return Directory
     */
    public abstract UniversalFile getRuntimeDirectory();

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
