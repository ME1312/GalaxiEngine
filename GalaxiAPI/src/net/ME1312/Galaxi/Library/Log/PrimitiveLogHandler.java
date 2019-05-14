package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Library.Util;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static java.util.logging.Level.*;

/**
 * Primitive Log Handler Class
 */
public final class PrimitiveLogHandler extends Handler {
    private final Logger log;
    private boolean open = true;

    PrimitiveLogHandler(Logger log) {
        this.log = log;
    }

    @Override
    public void publish(LogRecord record) {
        if (open) {
            LogStream stream = log.info;

            if (record.getLevel().intValue() == OFF.intValue()) {
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
                for (Object obj : record.getParameters()) {
                    message = message.replace("{" + i + "}", Util.getDespiteException(() -> (obj == null)?"null":obj.toString(), ""));
                    i++;
                }
                stream.println(message);
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
