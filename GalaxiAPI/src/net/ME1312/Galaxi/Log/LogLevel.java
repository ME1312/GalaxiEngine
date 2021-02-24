package net.ME1312.Galaxi.Log;

/**
 * Log Level Enum
 */
public enum LogLevel {
    DEBUG  ("96"),
    MESSAGE("90"),
    INFO   (null),
    SUCCESS("92"),
    WARN   ("93"),
    ERROR  ("91"),
    SEVERE ("91"),
    ;
    private String name;
    private String color;
    LogLevel(String color) {
        this(color, null);
    }
    LogLevel(String color, String name) {
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
    String getColor() {
        return color;
    }
}
