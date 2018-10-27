package net.ME1312.Galaxi.Engine.Library.Log;

import jline.console.CursorBuffer;
import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.Library.ConsoleReader;
import org.fusesource.jansi.Ansi;

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
    private LinkedList<Integer> buffer;
    private CursorBuffer hidden;

    protected ConsoleStream(jline.console.ConsoleReader jline, PrintStream original) {
        this.jline = jline;
        this.buffer = new LinkedList<Integer>();
        this.original = original;
    }

    @Override
    public void write(int i) {
        try {
            if (i == '\n') {
                LinkedList<Integer> buffer = this.buffer;
                this.buffer = new LinkedList<Integer>();
                hide();
                for (Integer b : buffer) {
                    original.write(b);
                    writeToWindow(b);
                }
                original.print(Ansi.ansi().a(Ansi.Attribute.RESET).toString());
                original.write(i);
                writeToWindow(i);
                show();
            } else {
                buffer.add(i);
            }
        } catch (Exception e) {}
    }

    private void writeToWindow(int i) {
        ConsoleReader reader = GalaxiEngine.getInstance().getConsoleReader();
        if (reader != null) try {
            Field f = ConsoleReader.class.getDeclaredField("window");
            f.setAccessible(true);
            if (f.get(reader) != null) {
                OutputStream window = (OutputStream) f.get(reader);
                window.write(i);
            }
            f.setAccessible(false);
        } catch (Exception e) {}
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
