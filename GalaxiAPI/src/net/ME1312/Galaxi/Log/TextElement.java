package net.ME1312.Galaxi.Log;

import net.ME1312.Galaxi.Library.Config.YAMLSection;
import net.ME1312.Galaxi.Library.Map.ObjectMap;
import net.ME1312.Galaxi.Library.Util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Text Element Builder Class
 */
public class TextElement {
    protected final LinkedList<TextElement> before = new LinkedList<TextElement>();
    protected final LinkedList<TextElement> prepend = new LinkedList<TextElement>();
    protected final LinkedList<TextElement> append = new LinkedList<TextElement>();
    protected final LinkedList<TextElement> after = new LinkedList<TextElement>();
    protected final ObjectMap<String> element;

    /**
     * Create a new Text Element
     */
    public TextElement() {
        this((String) null);
    }

    /**
     * Create a new Text Element
     *
     * @param text Text
     */
    public TextElement(String text) {
        this(generate(text));
    }
    private static ObjectMap<String> generate(String text) {
        ObjectMap<String> element = new ObjectMap<String>();
        if (text != null) element.set("msg", text);
        return element;
    }

    /**
     * Load a Text Element
     *
     * @param element Raw Element
     */
    public TextElement(ObjectMap<String> element) {
        Util.nullpo(element);
        this.element = element;
        load();
    }

    /**
     * Decode your text here
     */
    protected void load() {
        for (ObjectMap<String> e : element.getMapList("before", new LinkedList<ObjectMap<String>>())) before.add(new TextElement(e));
        for (ObjectMap<String> e : element.getMapList("pre", new LinkedList<ObjectMap<String>>())) prepend.add(new TextElement(e));
        for (ObjectMap<String> e : element.getMapList("post", new LinkedList<ObjectMap<String>>())) append.add(new TextElement(e));
        for (ObjectMap<String> e : element.getMapList("after", new LinkedList<ObjectMap<String>>())) after.add(new TextElement(e));
    }

    /**
     * Get the message
     *
     * @return Message
     */
    public String message() {
        return element.getString("msg");
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
     * Place elements before this element
     *
     * @param elements Elements to prepend
     * @return Text Element
     */
    public TextElement before(TextElement... elements) {
        for (int i = elements.length; i > 0;) before.addFirst(elements[--i]);
        return this;
    }

    /**
     * Place elements inside this element, but in front of the text
     *
     * @param elements Elements to insert
     * @return Text Element
     */
    public TextElement prepend(TextElement... elements) {
        for (int i = elements.length; i > 0;) prepend.addFirst(elements[--i]);
        return this;
    }

    /**
     * Add additional text before the existing text
     *
     * @param text Text to add
     * @return Text Element
     */
    public TextElement prepend(String... text) {
        StringBuilder msg = new StringBuilder(element.getString("msg"));
        for (int i = text.length; i > 0;) msg.insert(0, text[--i]);
        element.set("msg", msg.toString());
        return this;
    }

    /**
     * Add additional text behind the existing text
     *
     * @param text Text to add
     * @return Text Element
     */
    public TextElement append(String... text) {
        StringBuilder msg = new StringBuilder(element.getString("msg"));
        for (String s : text) msg.append(s);
        element.set("msg", msg.toString());
        return this;
    }

    /**
     * Place elements inside this element, but behind the text
     *
     * @param elements Elements to insert
     * @return Text Element
     */
    public TextElement append(TextElement... elements) {
        append.addAll(Arrays.asList(elements));
        return this;
    }

    /**
     * Place elements after this element
     *
     * @param elements Elements to append
     * @return Text Element
     */
    public TextElement after(TextElement... elements) {
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

    /**
     * Encode your text here
     *
     * @param stack Text Elements we've already seen before
     * @return Encoded Text
     */
    protected ObjectMap<String> toRaw(List<TextElement> stack) {
        stack.add(this);

        LinkedList<ObjectMap<String>> before = new LinkedList<ObjectMap<String>>();
        for (TextElement e : this.before) {
            if (stack.contains(e)) throw new IllegalStateException("Infinite text before loop");
            List<TextElement> p = new ArrayList<TextElement>();
            p.addAll(stack);
            before.add(e.toRaw(p));
        }
        if (!before.isEmpty()) element.set("before", before);
        else if (element.contains("before")) element.remove("before");

        LinkedList<ObjectMap<String>> prepend = new LinkedList<ObjectMap<String>>();
        for (TextElement e : this.prepend) {
            if (stack.contains(e)) throw new IllegalStateException("Infinite text prepend loop");
            List<TextElement> p = new ArrayList<TextElement>();
            p.addAll(stack);
            prepend.add(e.toRaw(p));
        }
        if (!prepend.isEmpty()) element.set("pre", prepend);
        else if (element.contains("pre")) element.remove("pre");

        LinkedList<ObjectMap<String>> append = new LinkedList<ObjectMap<String>>();
        for (TextElement e : this.append) {
            if (stack.contains(e)) throw new IllegalStateException("Infinite text append loop");
            List<TextElement> p = new ArrayList<TextElement>();
            p.addAll(stack);
            append.add(e.toRaw(p));
        }
        if (!append.isEmpty()) element.set("post", append);
        else if (element.contains("post")) element.remove("post");

        LinkedList<ObjectMap<String>> after = new LinkedList<ObjectMap<String>>();
        for (TextElement e : this.after) {
            if (stack.contains(e)) throw new IllegalStateException("Infinite text after loop");
            List<TextElement> p = new ArrayList<TextElement>();
            p.addAll(stack);
            after.add(e.toRaw(p));
        }
        if (!after.isEmpty()) element.set("after", after);
        else if (element.contains("after")) element.remove("after");

        return element.clone();
    }
}
