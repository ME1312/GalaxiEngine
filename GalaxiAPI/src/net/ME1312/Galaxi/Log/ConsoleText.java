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
public class ConsoleText extends TextElement {
    private static final HashMap<String, Runnable> callbacks = new HashMap<String, Runnable>();

    /**
     * Create a new Text Element
     */
    public ConsoleText() {}

    /**
     * Create a new Text Element
     *
     * @param text Text
     */
    public ConsoleText(String text) {
        super(text);
    }

    @Override
    protected void load() {
        for (ObjectMap<String> e : element.getMapList("before", new LinkedList<ObjectMap<String>>())) before.add(new ConsoleText(e));
        for (ObjectMap<String> e : element.getMapList("pre", new LinkedList<ObjectMap<String>>())) prepend.add(new ConsoleText(e));
        for (ObjectMap<String> e : element.getMapList("post", new LinkedList<ObjectMap<String>>())) append.add(new ConsoleText(e));
        for (ObjectMap<String> e : element.getMapList("after", new LinkedList<ObjectMap<String>>())) after.add(new ConsoleText(e));
    }

    /**
     * Load a Text Element
     *
     * @param element Raw Element
     */
    public ConsoleText(ObjectMap<String> element) {
        super(element);
    }

    @Override
    public ConsoleText bold(boolean value) {
        return (ConsoleText) super.bold(value);
    }

    @Override
    public ConsoleText italic(boolean value) {
        return (ConsoleText) super.italic(value);
    }

    @Override
    public ConsoleText underline(boolean value) {
        return (ConsoleText) super.underline(value);
    }

    @Override
    public ConsoleText strikethrough(boolean value) {
        return (ConsoleText) super.strikethrough(value);
    }

    /**
     * Set whether the text will be in superscript
     *
     * @param value Superscript Status
     * @return Text Element
     */
    public ConsoleText superscript(boolean value) {
        if (value && subscript()) subscript(false);
        element.set("sup", value);
        return this;
    }

    /**
     * Whether the text will be in superscript
     *
     * @return Superscript Status
     */
    public boolean superscript() {
        return element.getBoolean("sup", false);
    }

    /**
     * Set whether the text will be in subscript
     *
     * @param value Subscript Status
     * @return Text Element
     */
    public ConsoleText subscript(boolean value) {
        if (value && superscript()) superscript(false);
        element.set("sub", value);
        return this;
    }

    /**
     * Whether the text will be in subscript
     *
     * @return Subscript Status
     */
    public boolean subscript() {
        return element.getBoolean("sub", false);
    }

    /**
     * Set the color of the text
     *
     * @param color 8-bit Text Color
     * @return Text Element
     */
    public ConsoleText color(int color) {
        if (Logger.writer != null) {
            color(Logger.writer.parse256(color));
            element.getMap("c").set("8", color);
        }
        return this;
    }

    @Override
    public ConsoleText color(Color color) {
        return (ConsoleText) super.color(color);
    }


    /**
     * Set the background color of the text
     *
     * @param color 8-bit Background Color
     * @return Text Element
     */
    public ConsoleText backgroundColor(int color) {
        if (Logger.writer != null) {
            backgroundColor(Logger.writer.parse256(color));
            element.getMap("bc").set("8", color);
        }
        return this;
    }

    /**
     * Set the background color of the text
     *
     * @param color Text Background Color (or null for default)
     * @return Text Element
     */
    public ConsoleText backgroundColor(Color color) {
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
    public ConsoleText onClick(Runnable value) {
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
    public ConsoleText onClick(URL value) {
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
    public ConsoleText before(TextElement... elements) {
        return (ConsoleText) super.before(elements);
    }

    @Override
    public ConsoleText prepend(TextElement... elements) {
        return (ConsoleText) super.prepend(elements);
    }

    @Override
    public ConsoleText append(TextElement... elements) {
        return (ConsoleText) super.append(elements);
    }

    @Override
    public ConsoleText after(TextElement... elements) {
        return (ConsoleText) super.after(elements);
    }
}
