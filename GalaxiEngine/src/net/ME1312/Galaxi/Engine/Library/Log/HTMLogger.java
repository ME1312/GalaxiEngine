package net.ME1312.Galaxi.Engine.Library.Log;

import org.fusesource.jansi.AnsiOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.LinkedList;

public class HTMLogger extends AnsiOutputStream {
    private final String[] ANSI_COLOR_MAP = new String[]{"000000", "cd0000", "25bc24", "d7d700", "0000c3", "be00be", "00a5dc", "cccccc"};
    private final String[] ANSI_BRIGHT_COLOR_MAP = new String[]{"808080", "ff0000", "31e722", "ffff00", "0000ff", "ff00ff", "00c8ff", "ffffff"};
    private final byte[] BYTES_NBSP = "&nbsp;".getBytes();
    private final byte[] BYTES_QUOT = "&quot;".getBytes();
    private final byte[] BYTES_AMP = "&amp;".getBytes();
    private final byte[] BYTES_LT = "&lt;".getBytes();
    private final byte[] BYTES_GT = "&gt;".getBytes();
    private LinkedList<String> closingAttributes = new LinkedList<String>();
    protected boolean ansi = true;
    private boolean underline = false;
    private boolean strikethrough = false;

    public HTMLogger(OutputStream os) {
        super(os);
    }

    private void write(String s) throws IOException {
        super.out.write(s.getBytes());
    }

    private void writeAttribute(String s) throws IOException {
        this.write("<" + s + ">");
        this.closingAttributes.add(0, s);
    }

    private void closeAttribute(String s) throws IOException {
        LinkedList<String> closedAttributes = new LinkedList<String>();
        LinkedList<String> closingAttributes = new LinkedList<String>();
        LinkedList<String> unclosedAttributes = new LinkedList<String>();

        closingAttributes.addAll(this.closingAttributes);
        for (String attr : closingAttributes) {
            if (attr.toLowerCase().startsWith(s.toLowerCase())) {
                for (String a : unclosedAttributes) {
                    closedAttributes.add(0, a);
                    this.write("</" + a.split(" ", 2)[0] + ">");
                }
                this.closingAttributes.removeFirstOccurrence(attr);
                unclosedAttributes.clear();
                this.write("</" + attr.split(" ", 2)[0] + ">");
            } else {
                unclosedAttributes.add(attr);
            }
        }
        for (String attr : closedAttributes) {
            this.write("<" + attr + ">");
        }
    }

    private void closeAttributes() throws IOException {
        for (String attr : closingAttributes) {
            this.write("</" + attr.split(" ", 2)[0] + ">");
        }

        this.underline = false;
        this.strikethrough = false;
        this.closingAttributes.clear();
    }

    @Override
    protected void processDeleteLine(int amount) throws IOException {
        super.processDeleteLine(amount);
    }

    private boolean nbsp = true;
    @Override public void write(int data) throws IOException {
        if (data == 32) {
            if (nbsp) this.out.write(BYTES_NBSP);
            else super.write(data);
            nbsp = !nbsp;
        } else {
            nbsp = false;
            switch(data) {
                case 13:
                    break;
                case 34:
                    this.out.write(BYTES_QUOT);
                    break;
                case 38:
                    this.out.write(BYTES_AMP);
                    break;
                case 60:
                    this.out.write(BYTES_LT);
                    break;
                case 62:
                    this.out.write(BYTES_GT);
                    break;
                default:
                    super.write(data);
            }
        }
    }

    public void writeLine(byte[] buf, int offset, int len) throws IOException {
        this.write(buf, offset, len);
        this.closeAttributes();
    }

    private String parseTextDecoration() {
        String dec = "";
        if (underline) dec += " underline";
        if (strikethrough) dec += " line-through";
        if (dec.length() <= 0) dec += " none";

        return dec.substring(1);
    }

    @Override
    protected void processSetAttribute(int attribute) throws IOException {
        if (ansi) switch(attribute) {
            case 1:
                this.closeAttribute("b");
                this.writeAttribute("b");
                break;
            case 3:
                this.closeAttribute("i");
                this.writeAttribute("i");
                break;
            case 4:
                this.closeAttribute("span class=\"ansi-decoration");
                this.underline = true;
                this.writeAttribute("span class=\"ansi-decoration\" style=\"text-decoration: " + parseTextDecoration() + ";\"");
                break;
            case 9:
                this.closeAttribute("span class=\"ansi-decoration");
                this.strikethrough = true;
                this.writeAttribute("span class=\"ansi-decoration\" style=\"text-decoration: " + parseTextDecoration() + ";\"");
                break;
            case 22:
                this.closeAttribute("b");
                break;
            case 23:
                this.closeAttribute("i");
                break;
            case 24:
                this.closeAttribute("span class=\"ansi-decoration");
                this.underline = false;
                this.writeAttribute("span class=\"ansi-decoration\" style=\"text-decoration: " + parseTextDecoration() + ";\"");
                break;
            case 29:
                this.closeAttribute("span class=\"ansi-decoration");
                this.strikethrough = false;
                this.writeAttribute("span class=\"ansi-decoration\" style=\"text-decoration: " + parseTextDecoration() + ";\"");
                break;
        }
    }

    @Override
    protected void processUnknownOperatingSystemCommand(int label, String arg) {
        try {
            if (ansi) switch (label) {
                case 99900: // Galaxi Console Exclusives 99900-99999
                    this.closeAttribute("a");
                    this.writeAttribute("a href=\"" + URLDecoder.decode(arg, "UTF-8") + "\" target=\"_blank\"");
                    break;
                case 99901:
                    this.closeAttribute("a");
                    break;
            }
        } catch (IOException e) {}
    }

    @Override
    protected void processAttributeRest() throws IOException {
        this.closeAttributes();
    }

    private String parse8BitColor(int color) throws IOException {
        if (color < 8) {
            return ANSI_COLOR_MAP[color];
        } else if (color < 16) {
            return ANSI_BRIGHT_COLOR_MAP[color - 8];
        } else if (color < 232) {
            int r = (int) (Math.floor((color - 16) / 36d) * (255 / 5));
            int g = (int) (Math.floor(((color - 16) % 36d) / 6d) * (255 / 5));
            int b = (int) (Math.floor(((color - 16) % 36d) % 6d) * (255 / 5));
            return ((r >= 16)?"":"0") + Integer.toString(r, 16) + ((g >= 16)?"":"0") + Integer.toString(g, 16) + ((b >= 16)?"":"0") + Integer.toString(b, 16);
        } else if (color < 256) {
            int gray = (int) ((255 / 25d) * (color - 232 + 1));
            return ((gray >= 16)?"":"0") + Integer.toString(gray, 16) + ((gray >= 16)?"":"0") + Integer.toString(gray, 16) + ((gray >= 16)?"":"0") + Integer.toString(gray, 16);
        } else {
            throw new IOException("Invalid 8 Bit Color: " + color);
        }
    }

    @Override
    protected void processDefaultTextColor() throws IOException {
        this.closeAttribute("span class=\"ansi-foreground");
    }

    @Override
    protected void processSetForegroundColor(int color) throws IOException {
        processSetForegroundColor(color, false);
    }

    @Override
    protected void processSetForegroundColor(int color, boolean bright) throws IOException {
        if (ansi) {
            processDefaultTextColor();
            this.writeAttribute("span class=\"ansi-foreground\" style=\"color: #" + ((!bright)?ANSI_COLOR_MAP:ANSI_BRIGHT_COLOR_MAP)[color] + ";\"");
        }
    }

    @Override
    protected void processSetForegroundColorExt(int index) throws IOException {
        if (ansi) {
            processDefaultTextColor();
            this.writeAttribute("span class=\"ansi-foreground\" style=\"color: #" + parse8BitColor(index) + ";\"");
        }
    }

    @Override
    protected void processSetForegroundColorExt(int r, int g, int b) throws IOException {
        if (ansi) {
            processDefaultTextColor();
            this.writeAttribute("span class=\"ansi-foreground\" style=\"color: #" + ((r >= 16)?"":"0") + Integer.toString(r, 16) + ((g >= 16)?"":"0") + Integer.toString(g, 16) + ((b >= 16)?"":"0") + Integer.toString(b, 16) + ";\"");
        }
    }

    @Override
    protected void processDefaultBackgroundColor() throws IOException {
        this.closeAttribute("span class=\"ansi-background");
    }

    @Override
    protected void processSetBackgroundColor(int color) throws IOException {
        processSetBackgroundColor(color, false);
    }

    @Override
    protected void processSetBackgroundColor(int color, boolean bright) throws IOException {
        if (ansi) {
            processDefaultBackgroundColor();
            this.writeAttribute("span class=\"ansi-background\" style=\"background-color: #" + ((!bright)?ANSI_COLOR_MAP:ANSI_BRIGHT_COLOR_MAP)[color] + ";\"");
        }
    }

    @Override
    protected void processSetBackgroundColorExt(int index) throws IOException {
        if (ansi) {
            processDefaultBackgroundColor();
            this.writeAttribute("span class=\"ansi-background\" style=\"background-color: #" + parse8BitColor(index) + ";\"");
        }
    }

    @Override
    protected void processSetBackgroundColorExt(int r, int g, int b) throws IOException {
        if (ansi) {
            processDefaultBackgroundColor();
            this.writeAttribute("span class=\"ansi-background\" style=\"background-color: #" + ((r >= 16)?"":"0") + Integer.toString(r, 16) + ((g >= 16)?"":"0") + Integer.toString(g, 16) + ((b >= 16)?"":"0") + Integer.toString(b, 16) + ";\"");
        }
    }

    @Override
    public void close() throws IOException {
        this.closeAttributes();
        super.close();
    }
}
