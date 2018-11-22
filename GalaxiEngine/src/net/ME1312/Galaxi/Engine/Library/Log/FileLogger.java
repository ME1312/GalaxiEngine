package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Engine.GalaxiOption;
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
    private static FileOutputStream writer = null;
    private static File file = null;
    private OutputStream origin;

    protected FileLogger(OutputStream origin) throws IOException {
        this.origin = origin;
        if (writer == null) {
            File dir = GalaxiOption.LOG_DIRECTORY.get();
            dir.mkdirs();

            int i = 1;
            try {
                for (File file : dir.listFiles()) {
                    if (Pattern.compile("^" + Pattern.quote(Galaxi.getInstance().getAppInfo().getName()) + " #\\d+ \\((?:\\d{1,2}-){2}\\d+\\)\\.log(?:\\.zip)?$").matcher(file.getName()).find()) i++;
                }
            } catch (Exception e) {}

            file = new File(dir,  Galaxi.getInstance().getAppInfo().getName() + " #" + i + " (" + new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime()) + ").log");
            writer = new FileOutputStream(file);
        }
    }

    @Override
    public void write(int b) throws IOException {
        if (b != '\u0000') {
            origin.write(b);
            if (writer != null) {
                if (b == '\n') writer.write('\r');
                writer.write(b);
                writer.flush();
            }
        }
    }

    /**
     * Get the file that is currently being written to
     *
     * @return Log File
     */
    public static File getFile() {
        return file;
    }

    private static void stop() {
        File compressed = (file != null)?new File(file.getParentFile(), file.getName() + ".zip"):null;
        try {
            if (writer != null) writer.close();
            if (file != null && compressed != null) {
                FileOutputStream fos = new FileOutputStream(compressed);
                Util.zip(file, fos);
                fos.flush();
                fos.close();
                file.delete();
            }
        } catch (Exception e) {
            if (compressed != null && !compressed.exists()) compressed.delete();
        }
        file = null;
        writer = null;
    }
}
