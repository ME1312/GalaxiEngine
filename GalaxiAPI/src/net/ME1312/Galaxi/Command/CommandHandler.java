package net.ME1312.Galaxi.Command;

/**
 * Command Executor Layout Class
 */
public interface CommandHandler {

    /**
     * Run Command
     *
     * @param sender Command Sender
     * @param handle Command Name
     * @param args Arguments
     */
    void command(CommandSender sender, String handle, String[] args);
}
