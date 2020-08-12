package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Library.Container.Container;
import net.ME1312.Galaxi.Library.Log.StringOutputStream;
import net.ME1312.Galaxi.Library.Util;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiString;
import org.jline.reader.LineReader;

import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_ANSI;

/**
 * Console Log Stream Class
 */
public class ConsoleStream extends StringOutputStream {
    private Container<OutputStream> window;
    private LineReader jline;
    private StringBuilder buffer;


    ConsoleStream(Container<OutputStream> window, LineReader jline) {
        this.window = window;
        this.jline = jline;
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(String s) {
        try {
            OutputStream window;
            if ((window = this.window.get()) != null) {
                Util.isException(() -> window.write(s.getBytes(StandardCharsets.UTF_8)));
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

                if (USE_ANSI.def()) {
                    jline.printAbove(buffer.toString().replace("\n", Ansi.ansi().a(Ansi.Attribute.RESET) + "\n"));
                } else jline.printAbove((String) new AnsiString(buffer.toString()).getPlain());

                jline.getTerminal().flush();
            } else {
                buffer.append(s);
            }
        } catch (Exception e) {}
    }
}
