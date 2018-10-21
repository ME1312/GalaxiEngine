package net.ME1312.Galaxi.Plugin.Command;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.ExtraDataHandler;
import net.ME1312.Galaxi.Library.Util;

import java.lang.reflect.InvocationTargetException;

/**
 * Command Sender Layout Class
 */
public interface CommandSender extends ExtraDataHandler {
    /**
     * Get Sender Name
     *
     * @return Sender Name
     */
    String getName();

    /**
     * Test if the Sender has a permission
     *
     * @param permission Permission to test
     * @return Sender's Permission Status
     */
    boolean hasPermission(String permission);

    /**
     * Send the Sender a message
     *
     * @param messages Messages to send
     */
    void sendMessage(String... messages);

    /**
     * Send a command as the Sender
     *
     * @param command Command to send
     */
    default void command(String command) {
        try {
            Class.forName("net.ME1312.Galaxi.Engine.Library.ConsoleReader").getMethod("runCommand", CommandSender.class, String.class).invoke(Util.getDespiteException(() -> {
                Class<?> engine = Class.forName("net.ME1312.Galaxi.Engine.GalaxiEngine");
                return engine.getMethod("getConsoleReader").invoke(engine.getMethod("getInstance").invoke(null));
            }, null), this, command);
        } catch (InvocationTargetException e) {
            Galaxi.getInstance().getAppInfo().getLogger().error.println(e.getTargetException());
        } catch (Exception e) {
            Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
        }
    }
}
