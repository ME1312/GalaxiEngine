package net.ME1312.Galaxi.Log;

/**
 * Log Level Enum
 */
public enum LogLevel {
    DEBUG  (80, "96"),
    MESSAGE(70, "90"),
    SUCCESS(60, "92"),
    INFO   (50, null),
    WARN   (40, "93"),
    ERROR  (30, "91"),
    SEVERE (20, "91"),
    ;
    private final byte id;
    private final String name;
    private final String color;
    LogLevel(int id, String color) {
        this(id, color, null);
    }
    LogLevel(int id, String color, String name) {
        this.id = (byte) id;
        this.name = (name != null)?name:toString();
        this.color = color;
    }

    /**
     * Get the ID of this Log Level
     *
     * @return Level ID
     */
    public byte getID() {
        return id;
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
