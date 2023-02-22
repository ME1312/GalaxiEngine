package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Directories;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Log.LogMessenger;

import org.fusesource.jansi.AnsiMode;
import org.fusesource.jansi.AnsiType;
import org.fusesource.jansi.io.AnsiOutputStream;

import java.awt.*;
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

final class FileLogger implements LogMessenger {
    private final FileOutputStream iwriter;
    private final FileOutputStream tmpwriter;
    private final OutputStream writer;
    private final boolean html;
    private final File file;
    private final File tmp;
    private final ConsoleLogger child;

    FileLogger(ConsoleLogger child) throws IOException {
        File dir = LOG_DIRECTORY.value();
        this.child = child;
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

        if (USE_LOG_FILE.value()) {
            dir.mkdirs();
            if (USE_RAW_LOG.value()) {
                html = false;
                file = new File(dir, name + ".log.txt");
                iwriter = new FileOutputStream(file);
                if ((USE_RAW_LOG_ANSI.value() == Boolean.TRUE) || (USE_RAW_LOG_ANSI.value() == null && USE_ANSI.value())) {
                    writer = iwriter;
                } else writer = new AnsiOutputStream(
                        iwriter, () -> Integer.MAX_VALUE,
                        AnsiMode.Strip, null,
                        AnsiType.Unsupported, null,
                        UTF_8,
                        null, null,
                        false
                );
            } else {
                html = true;
                file = new File(dir, name + ".log.htm");
                Util.copyFromJar(FileLogger.class.getClassLoader(), "net/ME1312/Galaxi/Engine/Runtime/Files/GalaxiLog.htm", file.getAbsolutePath());
                iwriter = new FileOutputStream(file, true);
                iwriter.write(("<h1>" + nameX.replace("<", "&lt;").replace(">", "&gt;").replace("&", "&amp;") + "</h1>\n").getBytes(UTF_8));
                iwriter.flush();
                writer = HTMLogger.wrap(iwriter, HTMLogger::new);
            }
        } else {
            html = false;
            file = null;
            writer = iwriter = null;
        }
    }
    public void log(String s) throws IOException {
        byte[] bytes = s.getBytes(UTF_8);
        child.log(s);
        writer.write(bytes);
        writer.flush();

        if (tmpwriter != null) {
            tmpwriter.write(bytes);
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
                writer.write("\u001B[m".getBytes(UTF_8));
                writer.flush();
                if (html) iwriter.write(("</body></html>").getBytes(UTF_8));
                writer.close();
            }
            if (file != null) {
                FileOutputStream fos = new FileOutputStream(compressed);
                Directories.zip(file, fos);
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
    }

    @Override
    public Color parse256(int color) {
        return Console.parse256(color);
    }
}
