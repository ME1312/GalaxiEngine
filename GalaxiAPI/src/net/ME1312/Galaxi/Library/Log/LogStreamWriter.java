package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Container;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * Log Stream Writer Class
 */
public class LogStreamWriter {
    private Container<OutputStream[]> out;
    private StringBuilder buffer = new StringBuilder();

    LogStreamWriter(Container<OutputStream[]> stream) {
        this.out = stream;
    }

    private void publish() throws IOException {
        String str = buffer.toString();
        buffer.append('\n');
        byte[] bytes = buffer.toString().getBytes(StandardCharsets.UTF_8);
        buffer = new StringBuilder();

        if (out.get() != null) for (OutputStream out : this.out.get()) {
            if (out instanceof PrintStream) {
                ((PrintStream) out).println(str);
            } else {
                out.write(bytes);
            }
            out.flush();
        }
    }

    public void write(String... str) throws IOException {
        for (String s : str) {
            if (s.contains("\n")) {
                do {
                    int i = s.indexOf("\n");
                    buffer.append(s, 0, i);
                    publish();
                    s = s.substring(i + 1);
                } while (s.contains("\n"));
                if (s.length() > 0)
                    buffer.append(s);
            } else {
                buffer.append(s);
            }
        }
    }

    public void write(char... c) throws IOException {
        write(new String(c));
    }
}
