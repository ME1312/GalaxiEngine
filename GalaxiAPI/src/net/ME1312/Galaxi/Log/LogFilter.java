package net.ME1312.Galaxi.Log;

/**
 * Log Filter Layout Class
 */
public interface LogFilter {

    /**
     * Decide whether or not a message should be logged
     *
     * @param stream Stream that sent this message
     * @param message Message to be logged
     * @return true for a definite yes, false for no, or null for undecided (pass it on to the next filter)
     */
    Boolean filter(LogStream stream, String message);
}
