package net.ME1312.Galaxi.Engine.Library.Log;

import jline.console.CursorBuffer;
import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.Library.ConsoleReader;
import org.fusesource.jansi.Ansi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.LinkedList;

/**
 * Console Log Stream Class
 */
public class ConsoleStream extends OutputStream {
    private jline.console.ConsoleReader jline;
    private PrintStream original;
    private ByteArrayOutputStream buffer;
    private CursorBuffer hidden;

    protected ConsoleStream(jline.console.ConsoleReader jline, PrintStream original) {
        this.jline = jline;
        this.buffer = new ByteArrayOutputStream();
        this.original = original;
    }

    @Override
    public void write(int i) {
        try {
            if (i == '\n') {
                byte[] buffer = this.buffer.toByteArray();
                this.buffer = new ByteArrayOutputStream();
                OutputStream window = getWindow();
                hide();
                if (window != null) {
                    window.write(buffer);
                    window.write(i);
                }
                original.write(buffer);
                original.print(Ansi.ansi().a(Ansi.Attribute.RESET).toString());
                original.write(i);
                show();
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

    private void hide() {
        hidden = jline.getCursorBuffer().copy();
        try {
            jline.getOutput().write("\u001b[1G\u001b[K");
            jline.flush();
        } catch (IOException e) {}
    }

    private void show() {
        try {
            jline.resetPromptLine(jline.getPrompt(), hidden.toString(), hidden.cursor);
            jline.flush();
        } catch (IOException e) {}
    }
}
