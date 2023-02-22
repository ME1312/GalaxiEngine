package net.ME1312.Galaxi.Engine;

import net.ME1312.Galaxi.Command.CommandProcessor;
import net.ME1312.Galaxi.Command.CommandSender;

import java.util.List;

/**
 * GalaxiEngine Command Parser Class
 */
public abstract class CommandParser extends CommandProcessor {

    /**
     * Complete a command
     *
     * @param command Parsed Command
     * @return Auto Completions
     */
    public abstract List<String> complete(CommandSender sender, Parsed command);

    /**
     * Run a command
     *
     * @param sender Command Sender
     * @param command Parsed Command
     * @return Whether the command was run
     */
    public abstract Status runCommand(CommandSender sender, Parsed command);

    /**
     * Parses a command
     *
     * @param command Command
     * @return Parsed Command
     */
    public abstract Parsed parseCommand(String command);

    /**
     * Parsed Command Class
     */
    public interface Parsed {
        /**
         * Escapes the next argument
         *
         * @param argument Argument to escape
         * @param complete Whether this argument should be finished off
         * @return Escaped Argument
         */
        CharSequence escape(CharSequence argument, boolean complete);

        /**
         * Get the current Word
         *
         * @return Current Word
         */
        String word();

        /**
         * Get the Word List
         *
         * @return Word List
         */
        List<String> words();

        /**
         * Get the current Word Cursor
         *
         * @return Word Cursor
         */
        int wordCursor();

        /**
         * Get the current Word Index
         *
         * @return Word Index
         */
        int wordIndex();

        /**
         * Get the current Raw Word
         *
         * @return Raw Word
         */
        String rawWord();

        /**
         * Get the Raw Word List
         *
         * @return Raw Word List
         */
        List<String> rawWords();

        /**
         * Get the current Raw Word Cursor
         *
         * @return Raw Word Cursor
         */
        int rawWordCursor();

        /**
         * Get the current Raw Word Index
         *
         * @return Raw Word Index
         */
        int rawWordLength();

        /**
         * Get the full Input Line
         *
         * @return Input Line
         */
        String line();

        /**
         * Get the full Input Line Cursor
         *
         * @return Input Line Cursor
         */
        int cursor();
    }
}
