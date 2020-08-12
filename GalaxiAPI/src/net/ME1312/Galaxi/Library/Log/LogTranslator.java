package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static java.util.logging.Level.*;

/**
 * Primitive Log Handler Class
 */
public class LogTranslator extends Handler {
    private final Logger log;
    private boolean open = true;

    /**
     * Create a Log Translator
     *
     * @param log Logger to output to
     */
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
                stream = log.severe;
            }

            if (stream != null) {
                String message = (record.getParameters() == null)? record.getMessage() : MessageFormat.format(record.getMessage(), record.getParameters());

                message = message.replace("\r", ""); // Remove carriage returns (they have special meaning in Galaxi)
                stream.println(message);
                if (record.getThrown() != null)
                    stream.println(record.getThrown());
            }
        }
    }

    private String convert(Object obj) {
        if (obj instanceof Throwable) {
            StringWriter sw = new StringWriter();
            ((Throwable) obj).printStackTrace(new PrintWriter(sw));
            return sw.toString();
        } else {
            return Util.getDespiteException(() -> (obj == null)?"null":obj.toString(), "err");
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
