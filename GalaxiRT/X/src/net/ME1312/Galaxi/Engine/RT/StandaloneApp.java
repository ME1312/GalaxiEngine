package net.ME1312.Galaxi.Engine.RT;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Log.Logger;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Galaxi Standalone App
 */
public class StandaloneApp {

    /**
     * Galaxi Standalone Launch Method
     *
     * @param args Command Line Arguments
     */
    public static void main(String[] args) {
        start();
    }

    private static void start() {
        try {
            if (Util.isException(GalaxiEngine::getInstance)) {
                Engine engine = new Engine(null);
                Logger log = engine.getAppInfo().getLogger();

                long begin = Calendar.getInstance().getTime().getTime();
                log.info.println("", "Searching for Plugins...");
                int loaded = engine.getPluginManager().loadPlugins(new File(engine.getRuntimeDirectory(), "Plugins"));
                log.info.println(loaded + " Plugin"+((loaded == 1)?"":"s") + " loaded in " + new DecimalFormat("0.000").format((Calendar.getInstance().getTime().getTime() - begin) / 1000D) + "s");
                
                engine.start(StandaloneApp::stop);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void stop() {
        Logger log = Galaxi.getInstance().getAppInfo().getLogger();
        log.info.println("Shutting down...");
    }
}
