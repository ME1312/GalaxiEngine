package net.ME1312.Galaxi.Log;

import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;

import java.awt.*;
import java.util.List;
import java.util.*;

/**
 * Text Element Builder Class
 */
public class TextElement {
    protected final LinkedList<TextElement> before = new LinkedList<TextElement>();
    protected final LinkedList<TextElement> after = new LinkedList<TextElement>();
    protected final ObjectMap<String> element;

    /**
     * Create a new Text Element
     *
     * @param text Text
     */
    public TextElement(String text) {
        this(generate(text));
    }
    private static YAMLSection generate(String text) {
        if (Util.isNull(text)) throw new NullPointerException();
        YAMLSection element = new YAMLSection();
        element.set("msg", text);
        return element;
    }

    /**
     * Load a Text Element (Override this constructor to add properties)
     *
     * @param element Raw Element
     */
    public TextElement(ObjectMap<String> element) {
        if (Util.isNull(element.getRawString("msg", null))) throw new NullPointerException();
        this.element = element;
        load();
    }

    /**
     * Populate the <i>before</i> and <i>after</i> maps here
     */
    protected void load() {
        for (ObjectMap<String> e : element.getMapList("pre", new LinkedList<ObjectMap<String>>())) before.add(new TextElement(e));
        for (ObjectMap<String> e : element.getMapList("post", new LinkedList<ObjectMap<String>>())) after.add(new TextElement(e));
    }

    /**
     * Get the message
     *
     * @return Message
     */
    public String message() {
        return element.getRawString("msg");
    }

    /**
     * Set whether the text will be bold
     *
     * @param value Bold Status
     * @return Text Element
     */
    public TextElement bold(boolean value) {
        element.set("b", value);
        return this;
    }

    /**
     * Whether the text will be bold
     *
     * @return Bold Status
     */
    public boolean bold() {
        return element.getBoolean("b", false);
    }

    /**
     * Set whether the text will be italic
     *
     * @param value Italic Status
     * @return Text Element
     */
    public TextElement italic(boolean value) {
        element.set("i", value);
        return this;
    }

    /**
     * Whether the text will be italic
     *
     * @return Italic Status
     */
    public boolean italic() {
        return element.getBoolean("i", false);
    }

    /**
     * Set whether the text will be underlined
     *
     * @param value Underline Status
     * @return Text Element
     */
    public TextElement underline(boolean value) {
        element.set("u", value);
        return this;
    }

    /**
     * Whether the text will be underlined
     *
     * @return Bold Status
     */
    public boolean underline() {
        return element.getBoolean("u", false);
    }

    /**
     * Set whether the text will be struck through
     *
     * @param value Strike through Status
     * @return Text Element
     */
    public TextElement strikethrough(boolean value) {
        element.set("s", value);
        return this;
    }

    /**
     * Whether the text will be struck through
     *
     * @return Strike through Status
     */
    public boolean strikethrough() {
        return element.getBoolean("s", false);
    }

    /**
     * Set the color of the text
     *
     * @param color Text Color (or null for default)
     * @return Text Element
     */
    public TextElement color(Color color) {
        if (color == null) {
            element.set("c", null);
        } else {
            YAMLSection c = new YAMLSection();
            c.set("r", color.getRed());
            c.set("g", color.getGreen());
            c.set("b", color.getBlue());
            c.set("a", color.getAlpha());
            element.set("c", c);
        }
        return this;
    }

    /**
     * Get the color of the text
     *
     * @return Text Color (or null for default)
     */
    public Color color() {
        if (element.getObject("c", null) == null) {
            return null;
        } else {
            ObjectMap<String> c = element.getMap("c");
            return new Color(c.getInt("r"), c.getInt("g"), c.getInt("b"), c.getInt("a"));
        }
    }

    /**
     * Prepend text elements to this element
     *
     * @param elements Elements to prepend
     * @return Text Element
     */
    public TextElement prepend(TextElement... elements) {
        LinkedList<TextElement> before = new LinkedList<TextElement>();
        before.addAll(Arrays.asList(elements));
        Collections.reverse(before);
        for (TextElement element : before) this.before.addFirst(element);
        return this;
    }

    /**
     * Append text elements to this element
     *
     * @param elements Elements to append
     * @return Text Element
     */
    public TextElement append(TextElement... elements) {
        after.addAll(Arrays.asList(elements));
        return this;
    }

    /**
     * Get the Raw Element
     *
     * @return Raw Element
     */
    public ObjectMap<String> toRaw() {
        return toRaw(new ArrayList<TextElement>());
    }
    private ObjectMap<String> toRaw(List<TextElement> past) {
        past.add(this);

        LinkedList<ObjectMap<String>> before = new LinkedList<ObjectMap<String>>();
        for (TextElement e : this.before) {
            if (past.contains(e)) throw new IllegalStateException("Infinite text prepend loop");
            List<TextElement> p = new ArrayList<TextElement>();
            p.addAll(past);
            before.add(e.toRaw(p));
        }
        if (!before.isEmpty()) element.set("pre", before);
        else if (element.contains("pre")) element.remove("pre");

        LinkedList<ObjectMap<String>> after = new LinkedList<ObjectMap<String>>();
        for (TextElement e : this.after) {
            if (past.contains(e)) throw new IllegalStateException("Infinite text append loop");
            List<TextElement> p = new ArrayList<TextElement>();
            p.addAll(past);
            after.add(e.toRaw(p));
        }
        if (!after.isEmpty()) element.set("post", after);
        else if (element.contains("post")) element.remove("post");

        return element.clone();
    }
}
