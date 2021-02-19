package net.ME1312.Galaxi.Log;

import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

/**
 * Console Text Element Builder Class
 */
public class ConsoleTextElement extends TextElement {
    private static final HashMap<String, Runnable> callbacks = new HashMap<String, Runnable>();

    /**
     * Create a new Text Element
     *
     * @param text Text
     */
    public ConsoleTextElement(String text) {
        super(text);
    }

    @Override
    protected void load() {
        for (ObjectMap<String> e : element.getMapList("pre", new LinkedList<ObjectMap<String>>())) before.add(new ConsoleTextElement(e));
        for (ObjectMap<String> e : element.getMapList("post", new LinkedList<ObjectMap<String>>())) after.add(new ConsoleTextElement(e));
    }

    /**
     * Load a Text Element (Override this constructor to add properties)
     *
     * @param element Raw Element
     */
    public ConsoleTextElement(ObjectMap<String> element) {
        super(element);
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

    /**
     * Set the background color of the text
     *
     * @param color Text Background Color (or null for default)
     * @return Text Element
     */
    public ConsoleTextElement backgroundColor(Color color) {
        if (color == null) {
            element.set("bc", null);
        } else {
            YAMLSection bc = new YAMLSection();
            bc.set("r", color.getRed());
            bc.set("g", color.getGreen());
            bc.set("b", color.getBlue());
            bc.set("a", color.getAlpha());
            element.set("bc", bc);
        }
        return this;
    }

    /**
     * Get the background color of the text
     *
     * @return Text Background Color (or null for default)
     */
    public Color backgroundColor() {
        if (element.getObject("bc", null) == null) {
            return null;
        } else {
            ObjectMap<String> bc = element.getMap("bc");
            return new Color(bc.getInt("r"), bc.getInt("g"), bc.getInt("b"), bc.getInt("a"));
        }
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
        }
        element.set("a", "mailto:execute@galaxi.engine?" + id);
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
            if (element.contains("a")) {
                return new URL(element.getRawString("a"));
            } else return null;
        } catch (MalformedURLException e) {
            return null;
        }
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