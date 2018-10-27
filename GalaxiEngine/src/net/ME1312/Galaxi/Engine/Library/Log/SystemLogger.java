package net.ME1312.Galaxi.Engine.Library.Log;

import jline.console.ConsoleReader;
import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.PluginManager;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Log.LogStream;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

/**
 * System.out and System.err Override Class
 */
public final class SystemLogger extends OutputStream {
    private HashMap<String, NamedContainer<LogStream, ByteArrayOutputStream>> last = new HashMap<String, NamedContainer<LogStream, ByteArrayOutputStream>>();
    private boolean error;

    protected SystemLogger(boolean level) {
        this.error = level;
    }

    private static void start(PrintStream out, PrintStream err, ConsoleReader in) throws Exception {
        if (Util.isNull(out, err)) throw new NullPointerException();
        Field f = Logger.class.getDeclaredField("pso");
        f.setAccessible(true);
        ((Container<PrintStream>) f.get(null)).set(new PrintStream(new FileLogger(new ConsoleStream(in, out)), false, "UTF-8"));
        f.setAccessible(false);

        f = Logger.class.getDeclaredField("pse");
        f.setAccessible(true);
        ((Container<PrintStream>) f.get(null)).set(new PrintStream(new FileLogger(new ConsoleStream(in, err)), false, "UTF-8"));
        f.setAccessible(false);

        System.setOut(new PrintStream(new SystemLogger(false), false, "UTF-8"));
        System.setErr(new PrintStream(new SystemLogger(true), false, "UTF-8"));
    }

    @SuppressWarnings("unchecked")
    private List<String> getKnownClasses() {
        List<String> value = null;
        try {
            Field f = PluginManager.class.getDeclaredField("knownClasses");
            f.setAccessible(true);
            value = (List<String>) f.get(GalaxiEngine.getInstance().getPluginManager());
            f.setAccessible(false);
        } catch (Exception e) {}
        return value;
    }

    @Override
    public void write(int c) throws IOException {
        int i = 0;
        String origin = java.lang.System.class.getCanonicalName();
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (i > 1 && getKnownClasses().contains(element.getClassName())) {
                origin = element.getClassName().replaceAll("\\$([^.$\\d]+)", ".$1").replaceAll("\\$[\\d]+", "");
                break;
            }
            i++;
        }
        if (!last.keySet().contains(origin)) {
            Logger log = new Logger(origin);
            last.put(origin, new NamedContainer<LogStream, ByteArrayOutputStream>((error)?log.error:log.info, new ByteArrayOutputStream()));
        }
        NamedContainer<LogStream, ByteArrayOutputStream> log = last.get(origin);
        log.get().write(c);
        if (c == '\n') {
            log.name().print(log.get().toString("UTF-8").replace("\r\n", "\n"));
            last.remove(origin);
        }
    }

    private static void stop() throws Exception {
        Thread.sleep(125);

        Field f = Logger.class.getDeclaredField("running");
        f.setAccessible(true);
        f.set(null, false);
        f.setAccessible(false);

        f = Logger.class.getDeclaredField("thread");
        f.setAccessible(true);
        Thread thread = (Thread) f.get(null);
        f.setAccessible(false);

        if (thread != null) while (thread.isAlive()) {
            Thread.sleep(125);
        }

        Method m = FileLogger.class.getDeclaredMethod("stop");
        m.setAccessible(true);
        m.invoke(null);
        m.setAccessible(false);
    }
}

