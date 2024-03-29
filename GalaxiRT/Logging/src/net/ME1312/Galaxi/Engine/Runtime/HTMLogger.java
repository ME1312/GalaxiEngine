package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Library.Container.Container;

import org.fusesource.jansi.AnsiColors;
import org.fusesource.jansi.AnsiMode;
import org.fusesource.jansi.AnsiType;
import org.fusesource.jansi.io.AnsiOutputStream;
import org.fusesource.jansi.io.AnsiProcessor;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Locale;

import static java.nio.charset.StandardCharsets.UTF_8;

class HTMLogger extends AnsiProcessor {
    private static final String[] ANSI_COLOR_MAP = new String[8];
    private static final String[] ANSI_BRIGHT_COLOR_MAP = new String[8];
    private static final byte[] BYTES_NBSP = "\u00A0".getBytes(UTF_8);
    private static final byte[] BYTES_AMP = "&amp;".getBytes(UTF_8);
    private static final byte[] BYTES_LT = "&lt;".getBytes(UTF_8);
    private LinkedList<String> currentAttributes = new LinkedList<String>();
    private LinkedList<String> queue = new LinkedList<String>();
    private OutputStream raw;
    boolean nbsp = false;
    private boolean underline = false;
    private boolean strikethrough = false;

    static AnsiOutputStream wrap(OutputStream raw, New constructor) {
        Container<HTMLogger> html = new Container<>();
        OutputStream wrapped = new OutputStream() {
            private boolean nbsp = false;

            @Override
            public void write(int data) throws IOException {
                HTMLogger htm = html.value;
                if (htm.queue.size() > 0) {
                    LinkedList<String> queue = htm.queue;
                    htm.queue = new LinkedList<>();
                    for (String attr : queue) {
                        htm.write('<' + attr + '>');
                        htm.currentAttributes.addFirst(attr);
                    }
                }

                if (data == 32) {
                    if (htm.nbsp) {
                        if (nbsp) raw.write(BYTES_NBSP);
                        else raw.write(data);
                        nbsp = !nbsp;
                    } else raw.write(data);
                } else {
                    nbsp = false;
                    switch(data) {
                        case '&':
                            raw.write(BYTES_AMP);
                            break;
                        case '<':
                            raw.write(BYTES_LT);
                            break;
                        case '\n':
                            htm.closeAttributes();
                        default:
                            raw.write(data);
                    }
                }
            }

            @Override
            public void flush() throws IOException {
                raw.flush();
            }

            @Override
            public void close() throws IOException {
                html.value.closeAttributes();
                raw.close();
            }
        };

        return new AnsiOutputStream(
                wrapped, () -> Integer.MAX_VALUE,
                AnsiMode.Default, html.value = constructor.value(raw, wrapped),
                AnsiType.Native, AnsiColors.TrueColor,
                UTF_8,
                null, null,
                true
        );
    }

    interface New {
        HTMLogger value(OutputStream raw, OutputStream wrapped);
    }

    HTMLogger(final OutputStream raw, OutputStream wrapped) {
        super(wrapped);
        this.raw = raw;
    }

    private void write(String s) throws IOException {
        raw.write(s.getBytes(UTF_8));
    }

    private void writeAttribute(String attr) throws IOException {
        queue.add(attr);
    }

    void closeAttribute(String s) throws IOException {

        // Try to remove a tag that doesn't exist yet first
        String[] queue = this.queue.toArray(new String[0]);
        for (int i = queue.length; i > 0;) {
            String attr = queue[--i];
            if (attr.toLowerCase().startsWith(s.toLowerCase())) {
                this.queue.removeLastOccurrence(attr);
                return;
            }
        }

        // Close a tag that we've already written
        LinkedList<String> closedAttributes = new LinkedList<String>();
        LinkedList<String> currentAttributes = new LinkedList<String>(this.currentAttributes);
        LinkedList<String> unclosedAttributes = new LinkedList<String>();

        for (String attr : currentAttributes) {
            if (attr.toLowerCase().startsWith(s.toLowerCase())) {
                for (String a : unclosedAttributes) {
                    closedAttributes.add(a);
                    this.currentAttributes.removeFirst();
                    write("</" + a.split(" ", 2)[0] + '>');
                }
                unclosedAttributes.clear();
                this.currentAttributes.removeFirst();
                write("</" + attr.split(" ", 2)[0] + '>');
                break;
            } else {
                unclosedAttributes.add(attr);
            }
        }

        // Queue unrelated tags to be re-opened
        for (String attr : closedAttributes) {
            this.queue.addFirst(attr);
        }
    }

    void closeAttributes() throws IOException {
        queue.clear();

        for (String attr : currentAttributes) {
            write("</" + attr.split(" ", 2)[0] + ">");
        }

        underline = false;
        strikethrough = false;
        currentAttributes.clear();
    }

    @Override
    protected void processDeleteLine(int amount) throws IOException {
        super.processDeleteLine(amount);
    }

    private void renderTextDecoration() throws IOException {
        String dec = "";
        if (underline) dec += " underline";
        if (strikethrough) dec += " line-through";

        closeAttribute("span style=\"text-decoration:");
        if (dec.length() != 0) writeAttribute("span style=\"text-decoration:" + dec.substring(1) + "\"");
    }

    @Override
    protected void processSetAttribute(int attribute) throws IOException {
        switch(attribute) {
            case 1:
                closeAttribute("b");
                writeAttribute("b");
                break;
            case 3:
                closeAttribute("i");
                writeAttribute("i");
                break;
            case 4:
                underline = true;
                renderTextDecoration();
                break;
            case 9:
                strikethrough = true;
                renderTextDecoration();
                break;
            case 22:
                closeAttribute("b");
                break;
            case 23:
                closeAttribute("i");
                break;
            case 24:
                underline = false;
                renderTextDecoration();
                break;
            case 29:
                strikethrough = false;
                renderTextDecoration();
                break;
            case 73:
                closeAttribute("su");
                writeAttribute("sup");
                break;
            case 74:
                closeAttribute("su");
                writeAttribute("sub");
                break;
            case 75:
                closeAttribute("su");
                break;
        }
    }

    @Override
    protected void processUnknownOperatingSystemCommand(int label, String arg) {
        try {
            if (label == 8) {
                closeAttribute("a");
                String[] args = arg.split(";", 3);
                if (args.length > 1 && args[1].length() > 0 && allowHyperlinks(args[1])) {
                    writeAttribute("a href=\"" + args[1].replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;") + "\" target=\"_blank\"");
                }
            }
        } catch (Exception e) {}
    }

    protected boolean allowHyperlinks(String link) {
        if (link.toLowerCase(Locale.ENGLISH).startsWith("mailto:execute@galaxi.engine")) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void processAttributeReset() throws IOException {
        closeAttributes();
    }

    static {
        int color = 0;
        for (; color < ANSI_COLOR_MAP.length; ++color) ANSI_COLOR_MAP[color] = parseColor(Console.ANSI_COLOR_MAP[color]);
        for (int i = 0; i < ANSI_BRIGHT_COLOR_MAP.length; ++i, ++color) ANSI_BRIGHT_COLOR_MAP[i] = parseColor(Console.ANSI_COLOR_MAP[color]);
    }

    private static String parseColor(Color color) {
        return ((color.getRed() >= 16)? "" : "0") + Integer.toString(color.getRed(), 16) + ((color.getGreen() >= 16)? "" : "0") + Integer.toString(color.getGreen(), 16) + ((color.getBlue() >= 16)? "" : "0") + Integer.toString(color.getBlue(), 16);
    }

    private static String parse256(int color) throws IOException {
        if (color < 8) {
            return ANSI_COLOR_MAP[color];
        } else if (color < 16) {
            return ANSI_BRIGHT_COLOR_MAP[color - 8];
        } else {
            return parseColor(Console.parse256(color));
        }
    }

    @Override
    protected void processDefaultTextColor() throws IOException {
        closeAttribute("span style=\"color:");
    }

    @Override
    protected void processSetForegroundColor(int color) throws IOException {
        processSetForegroundColor(color, false);
    }

    @Override
    protected void processSetForegroundColor(int color, boolean bright) throws IOException {
        processDefaultTextColor();
        writeAttribute("span style=\"color:#" + ((!bright)?ANSI_COLOR_MAP:ANSI_BRIGHT_COLOR_MAP)[color] + "\"");
        renderTextDecoration();
    }

    @Override
    protected void processSetForegroundColorExt(int index) throws IOException {
        processDefaultTextColor();
        writeAttribute("span style=\"color:#" + parse256(index) + "\"");
        renderTextDecoration();
    }

    @Override
    protected void processSetForegroundColorExt(int r, int g, int b) throws IOException {
        processDefaultTextColor();
        writeAttribute("span style=\"color:#" + ((r >= 16)?"":"0") + Integer.toString(r, 16) + ((g >= 16)?"":"0") + Integer.toString(g, 16) + ((b >= 16)?"":"0") + Integer.toString(b, 16) + "\"");
        renderTextDecoration();
    }

    @Override
    protected void processDefaultBackgroundColor() throws IOException {
        closeAttribute("span style=\"background-color:");
    }

    @Override
    protected void processSetBackgroundColor(int color) throws IOException {
        processSetBackgroundColor(color, false);
    }

    @Override
    protected void processSetBackgroundColor(int color, boolean bright) throws IOException {
        processDefaultBackgroundColor();
        writeAttribute("span style=\"background-color:#" + ((!bright)?ANSI_COLOR_MAP:ANSI_BRIGHT_COLOR_MAP)[color] + "\"");
    }

    @Override
    protected void processSetBackgroundColorExt(int index) throws IOException {
        processDefaultBackgroundColor();
        writeAttribute("span style=\"background-color:#" + parse256(index) + "\"");
    }

    @Override
    protected void processSetBackgroundColorExt(int r, int g, int b) throws IOException {
        processDefaultBackgroundColor();
        writeAttribute("span style=\"background-color:#" + ((r >= 16)?"":"0") + Integer.toString(r, 16) + ((g >= 16)?"":"0") + Integer.toString(g, 16) + ((b >= 16)?"":"0") + Integer.toString(b, 16) + "\"");
    }
}
