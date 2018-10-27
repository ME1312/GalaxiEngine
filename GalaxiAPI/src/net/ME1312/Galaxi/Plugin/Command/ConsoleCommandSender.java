package net.ME1312.Galaxi.Plugin.Command;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Config.YAMLValue;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.TextElement;
import org.fusesource.jansi.Ansi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

    @SuppressWarnings("unchecked")
    @Override
    public void sendMessage(TextElement... messages) {
        for (TextElement original : messages) {
            TextElement element = new TextElement(original.toRaw());
            try {
                Field f = TextElement.class.getDeclaredField("before");
                f.setAccessible(true);
                for (TextElement e : (LinkedList<TextElement>) f.get(element)) sendMessage(e);
                f.setAccessible(false);
            } catch (Throwable e) {
                Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
            }

            StringBuilder message = new StringBuilder();
            if (element.bold()) message.append("\u001B[1m");
            if (element.italic()) message.append("\u001B[3m");
            if (element.underline()) message.append("\u001B[4m");
            if (element.strikethrough()) message.append("\u001B[9m");
            if (element.color() != null) {
                int red = element.color().getRed();
                int green = element.color().getGreen();
                int blue = element.color().getBlue();
                int alpha = element.color().getAlpha();

                red = Math.round((alpha * (red / 255f)) * 255);
                green = Math.round((alpha * (green / 255f)) * 255);
                blue = Math.round((alpha * (blue / 255f)) * 255);

                //noinspection StringConcatenationInsideStringBufferAppend
                message.append("\u001B[38;2;" + red + ";" + green + ";" + blue + "m");
            }
            message.append(element.message());
            message.append(Ansi.ansi().reset().toString());
            Galaxi.getInstance().getAppInfo().getLogger().message.println(message.toString());

            try {
                Field f = TextElement.class.getDeclaredField("after");
                f.setAccessible(true);
                for (TextElement e : (LinkedList<TextElement>) f.get(element)) sendMessage(e);
                f.setAccessible(false);
            } catch (Throwable e) {
                Galaxi.getInstance().getAppInfo().getLogger().error.println(e);
            }
        }
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
