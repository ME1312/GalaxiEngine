package net.ME1312.Galaxi;

import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Util;

import java.awt.Color;
import java.util.*;

/**
 * Text Element Builder Class
 */
public class TextElement {
    private LinkedList<TextElement> before = new LinkedList<TextElement>();
    private LinkedList<TextElement> after = new LinkedList<TextElement>();
    private YAMLSection element;

    /**
     * Create a new Text Element
     *
     * @param text Text
     */
    public TextElement(String text) {
        this(create(text));
    }
    private static YAMLSection create(String text) {
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
    public TextElement(YAMLSection element) {
        if (Util.isNull(element.getRawString("msg", null))) throw new NullPointerException();
        this.element = element;
        for (YAMLSection e : element.getSectionList("pre", new LinkedList<YAMLSection>())) before.add(new TextElement(e));
        for (YAMLSection e : element.getSectionList("post", new LinkedList<YAMLSection>())) after.add(new TextElement(e));
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
            YAMLSection c = element.getSection("c");
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
    public YAMLSection toRaw() {
        return toRaw(new ArrayList<TextElement>());
    }
    private YAMLSection toRaw(List<TextElement> past) {
        past.add(this);

        LinkedList<YAMLSection> before = new LinkedList<YAMLSection>();
        for (TextElement e : this.before) {
            if (past.contains(e)) throw new IllegalStateException("Infinite text prepend loop");
            List<TextElement> p = new ArrayList<TextElement>();
            p.addAll(past);
            before.add(e.toRaw(p));
        }
        if (!before.isEmpty()) element.set("pre", before);
        else if (element.contains("pre")) element.remove("pre");

        LinkedList<YAMLSection> after = new LinkedList<YAMLSection>();
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

    @Override
    public String toString() {
        return toRaw().toJSON().toString();
    }
}
