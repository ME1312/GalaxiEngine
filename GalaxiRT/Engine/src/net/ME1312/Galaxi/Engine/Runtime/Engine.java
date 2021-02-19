package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.GalaxiOption;
import net.ME1312.Galaxi.Event.Engine.GalaxiStartEvent;
import net.ME1312.Galaxi.Event.Engine.GalaxiStopEvent;
import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Platform;
import net.ME1312.Galaxi.Library.UniversalFile;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Library.Version.Version;
import net.ME1312.Galaxi.Log.LogLevel;
import net.ME1312.Galaxi.Log.Logger;
import net.ME1312.Galaxi.Plugin.App;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import org.json.JSONObject;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.jar.Manifest;

import static net.ME1312.Galaxi.Engine.GalaxiOption.*;

@App(name = "GalaxiEngine", version = "3.6.0a", authors = "ME1312", description = "An engine for command line Java applications", website = "https://github.com/ME1312/GalaxiEngine")
class Engine extends GalaxiEngine {
    private final UniversalFile dir = new UniversalFile(RUNTIME_DIRECTORY.app());
    private final UniversalFile idir;

    private final PluginInfo app;
    private final PluginInfo engine;
    final CodeManager code = new CodeManager(this);
    final Console console;

    volatile boolean running = false;
    volatile boolean stopping = false;
    private Runnable onStop = null;

    public static Engine getInstance() {
        return (Engine) instance;
    }


    Engine(PluginInfo app) throws Exception {
        System.setProperty("apple.laf.useScreenMenuBar", "true");

        instance = this;
        this.engine = PluginInfo.load(this);
        this.app = (app == null)?engine:app;

        if (APPDATA_DIRECTORY.app() == Platform.getSystem().getAppDataDirectory()) APPDATA_DIRECTORY.value(new File(Platform.getSystem().getAppDataDirectory(), this.getAppInfo().getName()));
        Util.reflect(GalaxiOption.class.getDeclaredField("lock"), null, true);

        this.idir = new UniversalFile(APPDATA_DIRECTORY.app());

        Manifest manifest = new Manifest(Engine.class.getResourceAsStream("/META-INF/GalaxiEngine.MF"));
        if (manifest.getMainAttributes().getValue("Implementation-Version") != null && manifest.getMainAttributes().getValue("Implementation-Version").length() > 0)
            engine.setBuild(new Version(manifest.getMainAttributes().getValue("Implementation-Version")));

        Util.isException(() -> engine.setIcon(Engine.class.getResourceAsStream("/net/ME1312/Galaxi/Engine/Runtime/Files/GalaxiIcon.png")));
        Util.isException(() -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()));
        code.catalogLibrary(engine.get().getClass());
        code.catalogLibrary(this.app.get().getClass());

        if (!(SHOW_DEBUG_MESSAGES.usr().equalsIgnoreCase("true") || (SHOW_DEBUG_MESSAGES.usr().length() <= 0 && SHOW_DEBUG_MESSAGES.app())))
            Logger.addStaticFilter((stream, message) -> (stream.getLevel() != LogLevel.DEBUG)?null:false);
        this.console = new Console(this);

        this.app.getLogger().info.println("Loading " + engine.getName() + " v" + engine.getVersion().toString() + " Libraries");
        if (app == null) this.app.getLogger().info.println("GalaxiEngine is running in standalone mode");
        else if (engine.getName().equalsIgnoreCase(this.app.getName())) throw new IllegalStateException("App name cannot be the same as the Engine's name");

        Commands.load(this);

        if (engine == this.app || !Engine.class.getProtectionDomain().getCodeSource().getLocation().equals(this.app.get().getClass().getProtectionDomain().getCodeSource().getLocation())) {
            engine.setUpdateChecker(() -> {
                try {
                    YAMLSection tags = new YAMLSection(new JSONObject("{\"tags\":" + Util.readAll(new BufferedReader(new InputStreamReader(new URL("https://api.github.com/repos/ME1312/GalaxiEngine/git/refs/tags").openStream(), Charset.forName("UTF-8")))) + '}'));
                    List<Version> versions = new LinkedList<Version>();

                    Version updversion = getEngineInfo().getVersion();
                    int updcount = 0;
                    for (ObjectMap<String> tag : tags.getMapList("tags")) versions.add(Version.fromString(tag.getString("ref").substring(10)));
                    Collections.sort(versions);
                    for (Version version : versions) {
                        if (version.compareTo(updversion) > 0) {
                            updversion = version;
                            updcount++;
                        }
                    }
                    if (updcount != 0) {
                        getAppInfo().getLogger().message.println(engine.getName() + " v" + updversion + " is available. You are " + updcount + " version" + ((updcount == 1)?"":"s") + " behind.");
                        return true;
                    }
                } catch (Exception e) {}
                return false;
            });
        }
    }

    @Override
    public void start(Runnable callback) {
        if (!running) {
            onStop = callback;
            running = true;
            console.thread.start();
            code.executeEvent(new GalaxiStartEvent(this));

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

    @Override
    public void stop(int code) {
        if (!stopping) {
            stopping = true;
            GalaxiStopEvent event = new GalaxiStopEvent(this, code);
            this.code.executeEvent(event);
            if (!event.isCancelled()) {
                exit(code);
            } else stopping = false;
        }
    }

    @Override
    public void terminate(int code) {
        stopping = true;
        GalaxiStopEvent event = new GalaxiStopEvent(this, code);
        this.code.executeEvent(event);
        exit(code);
    }

    private void exit(int code) {
        if (onStop != null) try {
            onStop.run();
        } catch (Throwable e) {
            app.getLogger().error.println(e);
        }

        running = false;
        Util.isException(SystemLogger::stop);
        System.exit(code);
    }

    @Override
    public Console getCommandProcessor() {
        return console;
    }

    @Override
    public CodeManager getPluginManager() {
        return code;
    }

    @Override
    public UniversalFile getAppDataDirectory() {
        return idir;
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
