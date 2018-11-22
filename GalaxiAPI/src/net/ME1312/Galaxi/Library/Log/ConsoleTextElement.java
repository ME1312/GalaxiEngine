package net.ME1312.Galaxi.Library.Log;

import net.ME1312.Galaxi.Galaxi;
import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Util;
import net.ME1312.Galaxi.TextElement;

import java.awt.*;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.UUID;

/**
 * Console Text Element Builder Class
 */
public class ConsoleTextElement extends TextElement {
    private static HashMap<String, Runnable> callbacks = new HashMap<String, Runnable>();
    private static boolean protocol = false;

    public ConsoleTextElement(String text) {
        super(text);
    }

    public ConsoleTextElement(YAMLSection element) {
        super(element);
    }

    /**
     * Run some code on click
     *
     * @param value Code to run
     * @return Text Element
     */
    public ConsoleTextElement onClick(Runnable value) {
        String id;
        if (callbacks.values().contains(value)) {
            id = Util.getBackwards(callbacks, value).get(0);
        } else {
            id = Util.getNew(callbacks.keySet(), UUID::randomUUID).toString();
            callbacks.put(id, value);
            Galaxi.getInstance().addProtocol(new URLStreamHandler() {
                @Override
                protected URLConnection openConnection(URL url) throws IOException {
                    return new URLConnection(url) {
                        @Override
                        public void connect() throws IOException {
                            callbacks.get(url.toString().substring(url.getProtocol().length() + 1)).run();
                        }
                    };
                }
            }, "galaxi.execute");
            protocol = true;
        }
        element.set("a", "galaxi.execute:" + id);
        return this;
    }

    /**
     * Open a link on click
     *
     * @param value Link to open
     * @return Text Element
     */
    public ConsoleTextElement onClick(URL value) {
        element.set("a", value);
        return this;
    }

    /**
     * Get the link that will open on click
     *
     * @return Link to open
     */
    public URL onClick() {
        try {
            return new URL(element.getRawString("a", null));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public ConsoleTextElement bold(boolean value) {
        return (ConsoleTextElement) super.bold(value);
    }

    @Override
    public ConsoleTextElement italic(boolean value) {
        return (ConsoleTextElement) super.italic(value);
    }

    @Override
    public ConsoleTextElement underline(boolean value) {
        return (ConsoleTextElement) super.underline(value);
    }

    @Override
    public ConsoleTextElement strikethrough(boolean value) {
        return (ConsoleTextElement) super.strikethrough(value);
    }

    @Override
    public ConsoleTextElement color(Color color) {
        return (ConsoleTextElement) super.color(color);
    }

    @Override
    public ConsoleTextElement prepend(TextElement... elements) {
        return (ConsoleTextElement) super.prepend(elements);
    }

    @Override
    public ConsoleTextElement append(TextElement... elements) {
        return (ConsoleTextElement) super.append(elements);
    }
}
