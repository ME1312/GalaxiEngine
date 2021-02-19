package net.ME1312.Galaxi.Command;

/**
 * Command AutoComplete Handler Layout Class
 *
 * @see Command
 */
public interface CompletionHandler {

    /**
     * Generate a list of completions for this command
     *
     * @param sender Command Sender
     * @param handle Command Handle
     * @param args Arguments (including the final unfinished argument)
     * @return AutoCompletion list
     */
    String[] complete(CommandSender sender, String handle, String[] args);
}
