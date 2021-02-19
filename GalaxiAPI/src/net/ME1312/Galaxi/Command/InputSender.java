package net.ME1312.Galaxi.Command;

import net.ME1312.Galaxi.Log.TextElement;

/**
 * Input Sender Layout Class
 */
public interface InputSender extends CommandSender {

    /**
     * Send a message as the Sender
     *
     * @param message Message to send
     */
    void chat(String message);

    /**
     * Send a message as the Sender
     *
     * @param message Message to send
     */
    void chat(TextElement message);
}
