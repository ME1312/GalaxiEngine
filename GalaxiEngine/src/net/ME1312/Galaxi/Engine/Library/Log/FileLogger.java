package net.ME1312.Galaxi.Engine.Library.Log;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Log.StringOutputStream;
import net.ME1312.Galaxi.Library.Util;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Pattern;

import static net.ME1312.Galaxi.Engine.GalaxiOption.LOG_DIRECTORY;
import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_LOG_FILE;
import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_RAW_LOG;

/**
 * Log File Writer Class
 */
public final class FileLogger extends StringOutputStream {
    private static FileOutputStream iwriter = null;
    private static FileOutputStream tmpwriter = null;
    private static OutputStream writer = null;
    private static File file = null;
    private static File tmp = null;
    private StringOutputStream origin;

    FileLogger(StringOutputStream origin) throws IOException {
        this.origin = origin;
        if (tmpwriter == null) {
            File dir = LOG_DIRECTORY.get();
            int i = 1;
            if (dir.isDirectory()) try {
                for (File file : dir.listFiles()) {
                    if (Pattern.compile("^" + Pattern.quote(Galaxi.getInstance().getAppInfo().getName()) + " #\\d+ \\((?:\\d{1,2}-){2}\\d+\\)\\.log(?:\\.(?:txt|htm|zip))?$").matcher(file.getName()).find()) i++;
                }
            } catch (Exception e) {}

            String name = Galaxi.getInstance().getAppInfo().getName() + " #" + i + " (" + new SimpleDateFormat("MM-dd-yyyy").format(Calendar.getInstance().getTime()) + ')';
            tmp = File.createTempFile(Galaxi.getInstance().getAppInfo().getName() + '.', ".log");
            tmp.deleteOnExit();
            tmpwriter = new FileOutputStream(tmp);

            if (USE_LOG_FILE.usr().equalsIgnoreCase("true") || (USE_LOG_FILE.usr().length() <= 0 && USE_LOG_FILE.get())) {
                dir.mkdirs();
                if (USE_RAW_LOG.usr().equalsIgnoreCase("true") || (USE_RAW_LOG.usr().length() <= 0 && USE_RAW_LOG.get())) {
                    file = new File(dir, name + ".log.txt");

                    writer = iwriter = new FileOutputStream(file);
                } else {
                    file = new File(dir, name + ".log.htm");
                    Util.copyFromJar(FileLogger.class.getClassLoader(), "net/ME1312/Galaxi/Engine/Library/Files/GalaxiLog.htm", file.getAbsolutePath());

                    iwriter = new FileOutputStream(file, true);
                    iwriter.write(("<h1>" + name + "</h1>\n").getBytes("UTF-8"));
                    iwriter.flush();
                    writer = HTMLogger.wrap(iwriter);
                }
            }
        }
    }

    @Override
    public void write(String s) throws IOException {
        origin.write(s);
        if (writer != null) {
            writer.write(s.getBytes(StandardCharsets.UTF_8));
            writer.flush();
        }
        if (tmpwriter != null) {
            tmpwriter.write(s.getBytes(StandardCharsets.UTF_8));
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
