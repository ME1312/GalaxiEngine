package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static java.util.logging.Level.*;

/**
 * Primitive Log Handler Class
 */
public final class LogTranslator extends Handler {
    private final Logger log;
    private boolean open = true;

    public LogTranslator(Logger log) {
        this.log = log;
    }

    @Override
    public void publish(LogRecord record) {
        if (open) {
            LogStream stream = log.info;

            if (record.getLevel().intValue() == OFF.intValue()) { // Translate Log Level
                stream = null;
            } else if (record.getLevel().intValue() == FINE.intValue() || record.getLevel().intValue() == FINER.intValue() || record.getLevel().intValue() == FINEST.intValue()) {
                stream = log.debug;
            } else if (record.getLevel().intValue() == ALL.intValue() || record.getLevel().intValue() == CONFIG.intValue() || record.getLevel().intValue() == INFO.intValue()) {
                // stream = log.info;
            } else if (record.getLevel().intValue() == WARNING.intValue()) {
                stream = log.warn;
            } else if (record.getLevel().intValue() == SEVERE.intValue()) {
                stream = log.error;
            }

            if (stream != null) {
                String message = record.getMessage();
                int i = 0;
                if (record.getParameters() != null) for (Object obj : record.getParameters()) {// Parse Parameters
                    String value;
                    if (obj instanceof Throwable) {
                        StringWriter sw = new StringWriter();
                        ((Throwable) obj).printStackTrace(new PrintWriter(sw));
                        value = sw.toString();
                    } else {
                        value = Util.getDespiteException(() -> (obj == null)?"null":obj.toString(), "err");
                    }

                    if (message.contains("{" + i + "}")) { // Write Parameters
                        message = message.replace("{" + i + "}", value);
                    } else if (message.contains("{}")) {
                        message = message.replaceFirst("\\{}", value);
                    } else {
                        message += "\n" + value;
                    }
                    i++;
                }

                message = message.replace("\r", ""); // Remove carriage returns (has special meaning in the Galaxi)
                for (String m : ((message.contains("\n"))?message.split("\n"):new String[]{ message })) stream.println(m); // Properly format new lines (if they exist)
            }
        }
    }

    @Override
    public void flush() {

    }

    @Override
    public void close() throws SecurityException {
        open = false;
    }
}
