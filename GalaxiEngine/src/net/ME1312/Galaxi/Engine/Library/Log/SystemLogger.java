package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.PluginManager;
import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Library.Log.LogStream;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.Log.StringOutputStream;
import net.ME1312.Galaxi.Library.Util;
import org.jline.reader.LineReader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static net.ME1312.Galaxi.Engine.GalaxiOption.COLOR_LOG_LEVELS;

/**
 * System.out and System.err Override Class
 */
public final class SystemLogger extends OutputStream {
    private static LineReader jline;
    private static Container<Boolean> jstatus;
    private HashMap<String, LogStream> last = new HashMap<String, LogStream>();
    private boolean error;

    SystemLogger(boolean level) {
        this.error = level;
    }

    private static void start(Container<OutputStream> window, LineReader jline, Container<Boolean> jstatus) throws Exception {
        if (Util.isNull(jline)) throw new NullPointerException();
        SystemLogger.jline = jline;
        SystemLogger.jstatus = jstatus;

        Util.<Container<StringOutputStream>>reflect(Logger.class.getDeclaredField("pso"), null).set(new FileLogger(new ConsoleStream(window, jline)));
        Util.reflect(Logger.class.getDeclaredMethod("log", boolean.class), null, COLOR_LOG_LEVELS.usr().equalsIgnoreCase("true") || (COLOR_LOG_LEVELS.usr().length() <= 0 && COLOR_LOG_LEVELS.get()));

        System.setOut(new PrintStream(new SystemLogger(false), false, "UTF-8"));
        System.setErr(new PrintStream(new SystemLogger(true), false, "UTF-8"));
    }

    @SuppressWarnings("unchecked")
    private List<String> getKnownClasses() {
        return new ArrayList<>(Util.<HashMap<String, ClassLoader>>getDespiteException(() -> Util.reflect(PluginManager.class.getDeclaredField("knownClasses"), GalaxiEngine.getInstance().getPluginManager()), null).keySet());
    }

    @Override
    public void write(int c) throws IOException {
        int i = 0;
        String origin = java.lang.System.class.getCanonicalName();
        for (StackTraceElement element : new Exception().getStackTrace()) {
            if (i > 1 && getKnownClasses().contains(element.getClassName())) {
                origin = element.getClassName().replaceAll("\\$([^.$\\d]+)", ".$1").replaceAll("\\$[\\d]+", "");
                break;
            }
            i++;
        }
        if (!last.keySet().contains(origin)) {
            Logger log = new Logger(origin);
            last.put(origin, (error)?log.error:log.info);
        }
        LogStream log = last.get(origin);
        log.toPrimitive().write(c);
        if (c == '\n') last.remove(origin);
    }

    private static void stop() throws Exception {
        Thread.sleep(125);

        Util.reflect(Logger.class.getDeclaredField("running"), null, false);

        Thread thread = Util.reflect(Logger.class.getDeclaredField("thread"), null);
        if (thread != null) while (thread.isAlive()) {
            Thread.sleep(125);
        }

        Util.reflect(FileLogger.class.getDeclaredMethod("stop"), null);
        if (jstatus.get()) {
            jline.callWidget(LineReader.CLEAR);
            jline.getTerminal().flush();
        }
    }
}

