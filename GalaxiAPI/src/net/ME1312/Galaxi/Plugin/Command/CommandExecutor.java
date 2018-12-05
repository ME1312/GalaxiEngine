package net.ME1312.Galaxi.Plugin.Command;

/**
 * Command Executor Layout Class
 */
public interface CommandExecutor {

    /**
     * Run Command
     *
     * @param sender Command Sender
     * @param handle Command Name
     * @param args Arguments
     */
    void command(CommandSender sender, String handle, String[] args);
}
