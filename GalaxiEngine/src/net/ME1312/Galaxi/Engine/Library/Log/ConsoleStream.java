package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.Library.ConsoleReader;
import net.ME1312.Galaxi.Library.Container;
import net.ME1312.Galaxi.Library.Util;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiString;
import org.jline.reader.LineReader;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_ANSI;

/**
 * Console Log Stream Class
 */
public class ConsoleStream extends OutputStream {
    private LineReader jline;
    private ByteArrayOutputStream buffer;


    ConsoleStream(LineReader jline) {
        this.jline = jline;
        this.buffer = new ByteArrayOutputStream();
    }

    @Override
    public void write(int i) {
        try {
            if (i == '\n') {
                byte[] buffer = this.buffer.toByteArray();
                this.buffer.reset();
                OutputStream window = getWindow();
                if (window != null) {
                    window.write(buffer);
                    window.write(i);
                }
                Container<Boolean> running = Util.reflect(GalaxiEngine.class.getDeclaredField("running"), GalaxiEngine.getInstance());
                if (running.get() && GalaxiEngine.getInstance().getConsoleReader().isAlive()) jline.callWidget(LineReader.CLEAR);
                if (USE_ANSI.def()) {
                    jline.getTerminal().writer().print(new String(buffer, StandardCharsets.UTF_8));
                    jline.getTerminal().writer().println(Ansi.ansi().a(Ansi.Attribute.RESET).toString());
                } else jline.getTerminal().writer().println((String) new AnsiString(new String(buffer, StandardCharsets.UTF_8)).getPlain());
                if (running.get() && GalaxiEngine.getInstance().getConsoleReader().isAlive()) {
                    jline.callWidget(LineReader.REDRAW_LINE);
                    jline.callWidget(LineReader.REDISPLAY);
                }
                jline.getTerminal().flush();
            } else {
                buffer.write(i);
            }
        } catch (Exception e) {}
    }

    private OutputStream getWindow() {
        ConsoleReader reader = GalaxiEngine.getInstance().getConsoleReader();
        OutputStream window = null;
        if (reader != null) try {
            Field f = ConsoleReader.class.getDeclaredField("window");
            f.setAccessible(true);
            if (f.get(reader) != null) {
                window = (OutputStream) f.get(reader);
            }
            f.setAccessible(false);
        } catch (Exception e) {}
        return window;
    }
}
