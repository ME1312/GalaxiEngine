package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Engine.GalaxiOption;
import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import static net.ME1312.Galaxi.Engine.GalaxiOption.LOG_DIRECTORY;
import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_RAW_LOG;

/**
 * Log File Writer Class
 */
public final class FileLogger extends OutputStream {
    private static FileOutputStream iwriter = null;
    private static FileOutputStream tmpwriter = null;
    private static OutputStream writer = null;
    private static File file = null;
    private static File tmp = null;
    private OutputStream origin;

    FileLogger(OutputStream origin) throws IOException {
        this.origin = origin;
        if (iwriter == null) {
            File dir = LOG_DIRECTORY.get();
            dir.mkdirs();

            int i = 1;
            try {
                for (File file : dir.listFiles()) {
                    if (Pattern.compile("^" + Pattern.quote(Galaxi.getInstance().getAppInfo().getName()) + " #\\d+ \\((?:\\d{1,2}-){2}\\d+\\)\\.log(?:\\.(?:txt|htm|zip))?$").matcher(file.getName()).find()) i++;
                }
            } catch (Exception e) {}

            String name = Galaxi.getInstance().getAppInfo().getName() + " #" + i + " (" + new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime()) + ')';
            tmp = File.createTempFile(Galaxi.getInstance().getAppInfo().getName() + '.', ".log");
            tmp.deleteOnExit();
            tmpwriter = new FileOutputStream(tmp);

            if (USE_RAW_LOG.usr().equalsIgnoreCase("true") || USE_RAW_LOG.get()) {
                file = new File(dir, name + ".log.txt");

                writer = iwriter = new FileOutputStream(file);
            } else {
                file = new File(dir, name + ".log.htm");
                Util.copyFromJar(FileLogger.class.getClassLoader(), "net/ME1312/Galaxi/Engine/Library/Files/GalaxiLog.htm", file.getAbsolutePath());

                iwriter = new FileOutputStream(file, true);
                iwriter.write(("<h1>" + name + "</h1>\n").getBytes("UTF-8"));
                iwriter.flush();
                writer = new HTMLogger(iwriter);
            }
        }
    }

    @Override
    public void write(int b) throws IOException {
        origin.write(b);
        if (writer != null) {
            writer.write(b);
            writer.flush();
        }
        if (tmpwriter != null) {
            tmpwriter.write(b);
            tmpwriter.flush();
        }
    }

    /**
     * Get the file that is currently being written to
     *
     * @return Log File
     */
    public static File getFile() {
        return tmp;
    }

    private static void stop() {
        File compressed = (file != null)?new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 4) + ".zip"):null;
        try {
            if (writer != null) {
                if (writer instanceof HTMLogger) iwriter.write(("</body>\n</html>").getBytes("UTF-8"));
                writer.close();
            }
            if (file != null && compressed != null) {
                FileOutputStream fos = new FileOutputStream(compressed);
                Util.zip(file, fos);
                fos.flush();
                fos.close();
                file.delete();
            }
            if (tmpwriter != null) {
                tmpwriter.close();
            }
        } catch (Exception e) {
            if (compressed != null && !compressed.exists()) compressed.delete();
        }

        if (tmp != null) tmp.delete();
        tmp = null;
        file = null;
        iwriter = null;
        writer = null;
    }
}
