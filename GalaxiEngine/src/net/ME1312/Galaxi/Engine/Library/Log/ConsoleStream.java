package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.Library.ConsoleReader;
import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Library.Log.StringOutputStream;
import net.ME1312.Galaxi.Library.Util;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiString;
import org.jline.reader.LineReader;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_ANSI;

/**
 * Console Log Stream Class
 */
public class ConsoleStream extends StringOutputStream {
    private LineReader jline;
    private StringBuilder buffer;


    ConsoleStream(LineReader jline) {
        this.jline = jline;
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(String s) {
        try {
            getWindow();
            if (window != null) {
                window.write(s.getBytes(StandardCharsets.UTF_8));
            }

            if (s.contains("\n")) {
                StringBuilder buffer = this.buffer;
                this.buffer = new StringBuilder();
                int i = s.lastIndexOf("\n") + 1;
                if (i < s.length()) {
                    buffer.append(s, 0, i);
                    this.buffer.append(s.substring(i));
                } else {
                    buffer.append(s);
                }

                getThread();
                Container<Boolean> running = Util.reflect(GalaxiEngine.class.getDeclaredField("running"), GalaxiEngine.getInstance());
                if (running.get() && thread.isAlive()) jline.callWidget(LineReader.CLEAR);

                if (USE_ANSI.def()) {
                    jline.getTerminal().writer().print(buffer.toString().replace("\n", Ansi.ansi().a(Ansi.Attribute.RESET) + "\n"));
                } else jline.getTerminal().writer().print((String) new AnsiString(buffer.toString()).getPlain());
                if (running.get() && thread.isAlive()) {
                    jline.callWidget(LineReader.REDRAW_LINE);
                    jline.callWidget(LineReader.REDISPLAY);
                }

                jline.getTerminal().flush();
            } else {
                buffer.append(s);
            }
        } catch (Exception e) {}
    }

    private static Thread thread;
    private static Thread getThread() {
        if (thread == null) {
            ConsoleReader reader = GalaxiEngine.getInstance().getConsoleReader();
            if (reader != null) try {
                Field f = ConsoleReader.class.getDeclaredField("thread");
                f.setAccessible(true);
                if (f.get(reader) != null) {
                    thread = (Thread) f.get(reader);
                }
                f.setAccessible(false);
            } catch (Exception e) {}
        }
        return thread;
    }

    private static OutputStream window;
    private static OutputStream getWindow() {
        if (window == null || Util.isException(window::flush)) {
            ConsoleReader reader = GalaxiEngine.getInstance().getConsoleReader();
            if (reader != null) try {
                Field f = ConsoleReader.class.getDeclaredField("window");
                f.setAccessible(true);
                if (f.get(reader) != null) {
                    window = (OutputStream) f.get(reader);
                }
                f.setAccessible(false);
            } catch (Exception e) {}
        }
        return window;
    }
}
