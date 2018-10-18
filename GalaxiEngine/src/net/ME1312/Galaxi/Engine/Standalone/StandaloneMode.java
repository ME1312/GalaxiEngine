package net.ME1312.Galaxi.Engine.Standalone;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Plugin.PluginInfo;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.util.Calendar;

/**
 * Galaxi Standalone App
 */
public class StandaloneMode {

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
            if (GalaxiEngine.getInstance() == null) {
                Constructor m = GalaxiEngine.class.getDeclaredConstructor(PluginInfo.class);
                m.setAccessible(true);
                GalaxiEngine engine = (GalaxiEngine) m.newInstance(new Object[]{null});
                m.setAccessible(false);

                Logger log = engine.getAppInfo().getLogger();

                long begin = Calendar.getInstance().getTime().getTime();
                log.info.println("", "Searching for Plugins...");
                int loaded = engine.getPluginManager().loadPlugins(new File(engine.getRuntimeDirectory(), "Plugins"));
                log.info.println(loaded + " Plugin"+((loaded == 1)?"":"s") + " loaded in " + new DecimalFormat("0.000").format((Calendar.getInstance().getTime().getTime() - begin) / 1000D) + "s");
                
                engine.start(StandaloneMode::stop);
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