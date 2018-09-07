package net.ME1312.Galaxi.Engine.Library.Log;

import jline.console.ConsoleReader;
import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.PluginManager;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Log.Logger;
import net.ME1312.Galaxi.Library.NamedContainer;
import net.ME1312.Galaxi.Library.Util;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.List;

/**
 * System.out and System.err Override Class
 */
public final class SystemLogger extends OutputStream {
    private NamedContainer<String, Logger> last = new NamedContainer<String, Logger>("", null);
    private boolean error;

    protected SystemLogger(boolean level) {
        this.error = level;
    }

    private static void start(PrintStream out, PrintStream err, ConsoleReader in) throws Exception {
        if (Util.isNull(out, err)) throw new NullPointerException();
        Field f = Logger.class.getDeclaredField("pso");
        f.setAccessible(true);
        f.set(null, new Container<PrintStream>(new PrintStream(new FileLogger(new ConsoleStream(in, out)))));
        f.setAccessible(false);

        f = Logger.class.getDeclaredField("pse");
        f.setAccessible(true);
        f.set(null, new Container<PrintStream>(new PrintStream(new FileLogger(new ConsoleStream(in, err)))));
        f.setAccessible(false);

        System.setOut(new PrintStream(new SystemLogger(false)));
        System.setErr(new PrintStream(new SystemLogger(true)));
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
    public void write(int c) {
        int i = 0;
        String origin = java.lang.System.class.getCanonicalName();
        for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
            if (i > 1 && getKnownClasses().contains(element.getClassName())) {
                origin = element.getClassName().replaceFirst("\\$.*", "");
                break;
            }
            i++;
        }
        if (!last.name().equals(origin)) last = new NamedContainer<String, Logger>(origin, new Logger(origin));
        if (error) {
            last.get().error.print((char) c);
        } else {
            last.get().info.print((char) c);
        }
    }

    private static void stop() {
        FileLogger.end();
    }
}

