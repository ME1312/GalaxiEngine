package net.ME1312.Galaxi.Library.Log;

import org.fusesource.jansi.Ansi;

/**
 * Log Level Enum
 */
public enum LogLevel {
    DEBUG(Ansi.ansi().fgBrightCyan()),
    MESSAGE(Ansi.ansi().fgBrightBlack()),
    INFO(),
    SUCCESS(Ansi.ansi().fgBrightGreen()),
    WARN(Ansi.ansi().fgBrightYellow()),
    ERROR(Ansi.ansi().fgBrightRed()),
    SEVERE(Ansi.ansi().fgBrightRed()),

    ;

    private String name;
    private Ansi color;
    LogLevel() {
        this(null, null);
    }
    LogLevel(String name) {
        this(name, null);
    }
    LogLevel(Ansi color) {
        this(null, color);
    }
    LogLevel(String name, Ansi color) {
        this.name = (name != null)?name:toString();
        this.color = color;
    }

    /**
     * Get the name of this Log Level
     *
     * @return Level Name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the color of this Log Level
     *
     * @return Level Color
     */
    public Ansi getColor() {
        return color;
    }
}
