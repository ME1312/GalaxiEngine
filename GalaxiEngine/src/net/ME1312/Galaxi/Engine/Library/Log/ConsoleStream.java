package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Engine.GalaxiEngine;
import net.ME1312.Galaxi.Engine.Library.ConsoleReader;

import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * Console Log Stream Class
 */
public class ConsoleStream extends OutputStream {

    ConsoleStream() {

    }

    @Override
    public void write(int i) {
        try {
            OutputStream window = getWindow();
            if (window != null) window.write(i);
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
