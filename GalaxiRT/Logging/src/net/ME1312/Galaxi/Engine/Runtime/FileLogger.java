package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Log.LogMessenger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.ME1312.Galaxi.Engine.GalaxiOption.*;
import static net.ME1312.Galaxi.Engine.Runtime.ConsoleLogger.ansi;

final class FileLogger implements LogMessenger {
    private FileOutputStream iwriter = null;
    private FileOutputStream tmpwriter = null;
    private OutputStream writer = null;
    private File file = null;
    private File tmp = null;
    private final ConsoleLogger child;

    FileLogger(ConsoleLogger child) throws IOException {
        this.child = child;
        if (tmpwriter == null) {
            File dir = LOG_DIRECTORY.app();
            int i = 1;
            if (dir.isDirectory()) try {
                for (File file : dir.listFiles()) {
                    if (Pattern.compile("^" + Pattern.quote(Galaxi.getInstance().getAppInfo().getName()) + " #\\d+ \\((?:\\d{1,2}-){2}\\d+\\)\\.log(?:\\.(?:txt|htm|zip))?$").matcher(file.getName()).find()) i++;
                }
            } catch (Exception e) {}

            Date time = Calendar.getInstance().getTime();
            String name = Galaxi.getInstance().getAppInfo().getName() + " #" + i + " (" + new SimpleDateFormat("MM-dd-yyyy").format(time) + ')';
            String nameX = Galaxi.getInstance().getAppInfo().getName() + " #" + i + " [" + new SimpleDateFormat("M/d/yyyy").format(time) + ']';
            tmp = File.createTempFile(Galaxi.getInstance().getAppInfo().getName() + '.', ".log");
            tmp.deleteOnExit();
            tmpwriter = new FileOutputStream(tmp);

            if (USE_LOG_FILE.usr().equalsIgnoreCase("true") || (USE_LOG_FILE.usr().length() <= 0 && USE_LOG_FILE.app())) {
                dir.mkdirs();
                if (USE_RAW_LOG.usr().equalsIgnoreCase("true") || (USE_RAW_LOG.usr().length() <= 0 && USE_RAW_LOG.app())) {
                    file = new File(dir, name + ".log.txt");

                    writer = iwriter = new FileOutputStream(file);
                } else {
                    file = new File(dir, name + ".log.htm");
                    Util.copyFromJar(FileLogger.class.getClassLoader(), "net/ME1312/Galaxi/Engine/Runtime/Files/GalaxiLog.htm", file.getAbsolutePath());

                    iwriter = new FileOutputStream(file, true);
                    iwriter.write(("<h1>" + nameX.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;") + "</h1>\n").getBytes(UTF_8));
                    iwriter.flush();
                    writer = HTMLogger.wrap(iwriter);
                }
            }
        }
    }

    public void log(String s) throws IOException {
        child.log(s);
        if (writer instanceof HTMLogger) {
            writer.write(s.getBytes(UTF_8));
            writer.flush();
        } else if (writer != null) {
            writer.write(ansi(s).getBytes(UTF_8));
            writer.flush();
        }
        if (tmpwriter != null) {
            tmpwriter.write(s.getBytes(UTF_8));
            tmpwriter.flush();
        }
    }

    File history() {
        return tmp;
    }

    void close() {
        File compressed = (file != null)?new File(file.getParentFile(), file.getName().substring(0, file.getName().length() - 4) + ".zip"):null;
        try {
            if (writer != null) {
                if (writer instanceof HTMLogger) iwriter.write(("</body></html>").getBytes(UTF_8));
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
