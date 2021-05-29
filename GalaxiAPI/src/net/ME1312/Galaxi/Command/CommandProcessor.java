package net.ME1312.Galaxi.Command;

import java.util.List;

/**
 * Galaxi Command Processor Class
 */
public abstract class CommandProcessor {

    // Console UI served by RT
    protected abstract Object openWindow(boolean exit);
    protected abstract Object getWindow();
    protected abstract void closeWindow(boolean exit);

    /**
     * Galaxi Command Status Class
     */
    public enum Status {
        SUCCESS,
        ERROR,
        UNKNOWN,
        CANCELLED
    }

    /**
     * Complete a command
     *
     * @param command Command
     * @return Auto Completions
     */
    public abstract List<String> complete(CommandSender sender, String command);

    /**
     * Run a command
     *
     * @param sender Command Sender
     * @param command Command
     * @return Whether the command was run
     */
    public abstract Status runCommand(CommandSender sender, String command);

    /**
     * Escapes a command
     *
     * @param label Command Label
     * @param args Command Arguments
     * @return Escaped Command
     */
    public String escapeCommand(String label, String... args) {
        return escapeCommand(label, args, false, false);
    }

    /**
     * Escapes some arguments
     *
     * @param args Command Arguments
     * @return Escaped Arguments
     */
    public String escapeArguments(String... args) {
        return escapeArguments(args, false, false);
    }

    /**
     * Escapes a command
     *
     * @param label Command Label
     * @param args Command Arguments
     * @param literal Literal String Escape Mode (using Single Quotes)
     * @param whitespaced Whitespaced String Escape Mode (using Double Quotes)
     * @return Escaped Command
     */
    public abstract String escapeCommand(String label, String[] args, boolean literal, boolean whitespaced);

    /**
     * Escapes some arguments
     *
     * @param args Command Arguments
     * @param literal Literal String Escape Mode (using Single Quotes)
     * @param whitespaced Whitespaced String Escape Mode (using Double Quotes)
     * @return Escaped Arguments
     */
    public abstract String escapeArguments(String[] args, boolean literal, boolean whitespaced);
}
