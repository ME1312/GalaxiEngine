package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

/**
 * Log File Writer Class
 */
public final class FileLogger extends OutputStream {
    private static FileWriter writer = null;
    private static File file = null;
    private PrintStream origin;

    protected FileLogger(PrintStream origin) throws IOException {
        this.origin = origin;
        if (writer == null) {
            File dir = new File(Galaxi.getInstance().getRuntimeDirectory(), "Logs");
            dir.mkdirs();

            int i = 1;
            try {
                for (File file : dir.listFiles()) {
                    if (Pattern.compile("^" + Pattern.quote(Galaxi.getInstance().getAppInfo().getName()) + " #\\d+ \\((?:\\d{1,2}-){2}\\d+\\)\\.log(?:\\.zip)?$").matcher(file.getName()).find()) i++;
                }
            } catch (Exception e) {}

            file = new File(dir,  Galaxi.getInstance().getAppInfo().getName() + " #" + i + " (" + new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime()) + ").log");
            writer = new FileWriter(file);
        }
    }

    @Override
    public void write(int b) throws IOException {
        origin.write((char)b);
        if (writer != null) {
            writer.write((char) b);
            writer.flush();
        }
    }

    static void end() {
        File compressed = new File(file.getParentFile(), file.getName() + ".zip");
        try {
            if (writer != null) writer.close();
            if (file != null) {
                FileOutputStream fos = new FileOutputStream(compressed);
                Util.zip(file, fos);
                fos.flush();
                fos.close();
                file.delete();
            }
        } catch (Exception e) {
            if (!compressed.exists()) compressed.delete();
        }
        file = null;
        writer = null;
    }
}
