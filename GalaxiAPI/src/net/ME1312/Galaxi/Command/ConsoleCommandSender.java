package net.ME1312.Galaxi.Command;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Map.ObjectMapValue;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.Log.TextElement;

/**
 * Console Command Sender Class
 */
public class ConsoleCommandSender implements CommandSender {
    private final ObjectMap<String> extra = new ObjectMap<String>();

    /**
     * Get the Console Command Sender
     *
     * @return Console Command Sender
     */
    public static ConsoleCommandSender get() {
        return Galaxi.getInstance().getConsole();
    }

    protected ConsoleCommandSender() {}

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    /**
     * Open the Console Window
     */
    public final void openWindow() {
        Galaxi.getInstance().getCommandProcessor().openWindow(false);
    }

    /**
     * Close the Console Window
     */
    public final void closeWindow() {
        Galaxi.getInstance().getCommandProcessor().closeWindow(false);
    }

    /**
     * Send a message as Console
     *
     * @param message Message to send
     * @see InputSender#chat(String) <i>ConsoleCommandSender</i> should be of type <i>InputSender</i> when this method is implemented
     * @throws UnsupportedOperationException when not implemented
     */
    public void chat(String message) {
        throw new UnsupportedOperationException("No console chat handler for type: String");
    }

    /**
     * Send a message as Console
     *
     * @param message Message to send
     * @see InputSender#chat(TextElement) <i>ConsoleCommandSender</i> should be of type <i>InputSender</i> when this method is implemented
     * @throws UnsupportedOperationException when not implemented
     */
    public void chat(TextElement message) {
        throw new UnsupportedOperationException("No console chat handler for type: TextElement");
    }

    @Override
    public void sendMessage(String... messages) {
        Galaxi.getInstance().getAppInfo().getLogger().message.println(messages);
    }

    @Override
    public void sendMessage(TextElement... messages) {
        Galaxi.getInstance().getAppInfo().getLogger().message.println(messages);
    }

    @Override
    public void addExtra(String handle, Object value) {
        Util.nullpo(handle, value);
        extra.set(handle, value);
    }

    @Override
    public boolean hasExtra(String handle) {
        Util.nullpo(handle);
        return extra.getKeys().contains(handle);
    }

    @Override
    public ObjectMapValue<String> getExtra(String handle) {
        Util.nullpo(handle);
        return extra.get(handle);
    }

    @Override
    public ObjectMap<String> getExtra() {
        return extra.clone();
    }

    @Override
    public void removeExtra(String handle) {
        Util.nullpo(handle);
        extra.remove(handle);
    }
}
