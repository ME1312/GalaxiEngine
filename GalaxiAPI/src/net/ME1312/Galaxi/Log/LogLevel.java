package net.ME1312.Galaxi.Log;

/**
 * Log Level Enum
 */
public enum LogLevel {
    DEBUG  ("\u001B[96m"),
    MESSAGE("\u001B[90m"),
    INFO   (    null    ),
    SUCCESS("\u001B[92m"),
    WARN   ("\u001B[93m"),
    ERROR  ("\u001B[91m"),
    SEVERE ("\u001B[91m"),
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
