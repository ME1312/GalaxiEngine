package net.ME1312.Galaxi.Plugin.Command;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Config.YAMLValue;
import net.ME1312.Galaxi.Library.Util;

/**
 * Console Command Sender Class
 */
public final class ConsoleCommandSender implements CommandSender {
    private static ConsoleCommandSender instance;
    private YAMLSection extra = new YAMLSection();

    /**
     * Get the Console Command Sender
     *
     * @return Console Command Sender
     */
    public static ConsoleCommandSender get() {
        if (instance == null) instance = new ConsoleCommandSender();
        return instance;
    }
    private ConsoleCommandSender() {}

    @Override
    public String getName() {
        return "CONSOLE";
    }

    @Override
    public boolean hasPermission(String permission) {
        return true;
    }

    @Override
    public void sendMessage(String... messages) {
        Galaxi.getInstance().getAppInfo().getLogger().message.println(messages);
    }

    @Override
    public void addExtra(String handle, Object value) {
        if (Util.isNull(handle, value)) throw new NullPointerException();
        extra.set(handle, value);
    }

    @Override
    public boolean hasExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return extra.getKeys().contains(handle);
    }

    @Override
    public YAMLValue getExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        return extra.get(handle);
    }

    @Override
    public YAMLSection getExtra() {
        return extra.clone();
    }

    @Override
    public void removeExtra(String handle) {
        if (Util.isNull(handle)) throw new NullPointerException();
        extra.remove(handle);
    }
}
