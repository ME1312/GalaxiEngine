package net.ME1312.Galaxi.Engine.RT;

import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Log.LogStream;
import net.ME1312.Galaxi.Log.Logger;

import org.jline.reader.LineReader;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static net.ME1312.Galaxi.Engine.GalaxiOption.COLOR_LOG_LEVELS;

public final class SystemLogger extends OutputStream {
    private final HashMap<String, LogStream> last = new HashMap<String, LogStream>();
    private static ExecutorService service;
    private static FileLogger file;
    private final boolean error;

    SystemLogger(boolean level) {
        this.error = level;
    }

    static void start(Console console) throws Exception {
        Util.reflect(Logger.class.getDeclaredField("writer"), null, file = new FileLogger(new ConsoleLogger(console)));
        Util.reflect(Logger.class.getDeclaredField("color"), null, COLOR_LOG_LEVELS.usr().equalsIgnoreCase("true") || (COLOR_LOG_LEVELS.usr().length() <= 0 && COLOR_LOG_LEVELS.app()));
        Util.reflect(Logger.class.getDeclaredField("service"), null, service = Executors.newSingleThreadExecutor(r -> new Thread(r, Engine.getInstance().getEngineInfo().getName() + "::Log_Spooler")));

        System.setOut(new PrintStream(new SystemLogger(false), false, "UTF-8"));
        System.setErr(new PrintStream(new SystemLogger(true), false, "UTF-8"));
    }

    static File history() {
        return file.history();
    }

    static void stop() throws Exception {
        Thread.sleep(125);

        Util.reflect(Logger.class.getDeclaredField("service"), null, null);
        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        file.close();

        Console console = Engine.getInstance().getCommandProcessor();
        if (console.jstatus) {
            console.jline.callWidget(LineReader.CLEAR);
            console.jline.getTerminal().flush();
        }
    }

    private String origin() {
        int i = 0;
        String origin = java.lang.System.class.getCanonicalName();
        for (StackTraceElement element : new Exception().getStackTrace()) {
            if (i > 1 && Engine.getInstance().code.knownClasses.containsKey(element.getClassName())) {
                origin = element.getClassName().replaceAll("\\$([^.$\\d]+)", ".$1").replaceAll("\\$[\\d]+", "");
                break;
            }
            i++;
        }
        return origin;
    }

    @Override
    public synchronized void write(int c) throws IOException {
        String origin = origin();
        if (!last.containsKey(origin)) {
            Logger log = new Logger(origin);
            last.put(origin, (error)?log.error:log.info);
        }
        LogStream log = last.get(origin);
        log.toPrimitive().write(c);
    }
}

