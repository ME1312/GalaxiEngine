package net.ME1312.Galaxi.Command;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.ExtraDataHandler;
import net.ME1312.Galaxi.Log.TextElement;

/**
 * Command Sender Layout Class
 */
public interface CommandSender extends ExtraDataHandler<String> {
    /**
     * Get Sender Name
     *
     * @return Sender Name
     */
    String getName();

    /**
     * Send the Sender a message
     *
     * @param messages Messages to send
     */
    void sendMessage(String... messages);

    /**
     * Send the Sender a message
     *
     * @param messages Messages to send
     */
    void sendMessage(TextElement... messages);

    /**
     * Test if the Sender has a permission
     *
     * @param permission Permission to test
     * @return Sender's Permission Status
     */
    boolean hasPermission(String permission);

    /**
     * Send a command as the Sender
     *
     * @param command Command to send
     */
    default void command(String command) {
        Galaxi.getInstance().getCommandProcessor().runCommand(this, command);
    }
}
