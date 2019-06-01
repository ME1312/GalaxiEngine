package net.ME1312.Galaxi.Engine.Library.Log;

import jline.console.CursorBuffer;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiString;

import java.io.*;

import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_ANSI;

/**
 * Terminal Log Wrapper Class
 */
public final class TerminalWrapper extends PrintStream {
    private jline.console.ConsoleReader jline;
    private CursorBuffer hidden;
    private PrintStream out;

    TerminalWrapper(jline.console.ConsoleReader in, PrintStream out) throws UnsupportedEncodingException {
        super(new BufferedOutputStream(out), false, "UTF-8");
        this.jline = in;
        this.out = out;
    }

    @Override
    public void flush() {
        // do nothing
    }

    @Override
    public void println(String s) {
        hide();
        if (USE_ANSI.def()) {
            out.print(s);
            out.print(Ansi.ansi().a(Ansi.Attribute.RESET).toString());
        } else out.print(new AnsiString(s).getPlain());
        out.print('\n');
        show();
    }

    private void hide() {
        hidden = jline.getCursorBuffer().copy();
        try {
            jline.getOutput().write("\u001b[1G\u001b[K");
            jline.flush();
        } catch (IOException e) {}
    }

    private void show() {
        try {
            jline.resetPromptLine(jline.getPrompt(), hidden.toString(), hidden.cursor);
            jline.flush();
        } catch (IOException e) {}
    }
}
