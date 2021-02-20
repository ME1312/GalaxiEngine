package net.ME1312.Galaxi.Engine.Runtime;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiString;

import static net.ME1312.Galaxi.Engine.GalaxiOption.USE_ANSI;

final class ConsoleLogger {
    private StringBuilder buffer;
    private final Console console;

    ConsoleLogger(Console console) {
        this.console = console;
        this.buffer = new StringBuilder();
    }

    void log(String s) {
        try {
            ConsoleUI window;
            if ((window = console.window) != null) {
                window.log(s);
            }

            if (s.contains("\n")) {
                StringBuilder buffer = this.buffer;
                this.buffer = new StringBuilder();
                int i = s.lastIndexOf('\n') + 1;
                if (i < s.length()) {
                    buffer.append(s, 0, i);
                    this.buffer.append(s.substring(i));
                } else {
                    buffer.append(s);
                }

                console.jline.printAbove(ansi(buffer.toString()));
                console.jline.getTerminal().flush();
            } else {
                buffer.append(s);
            }
        } catch (Exception e) {}
    }

    static String ansi(String s) {
        if (USE_ANSI.def()) {
            return s.replace("\n", "\n" + Ansi.ansi().a(Ansi.Attribute.RESET));
        } else return (String) new AnsiString(s).getPlain();
    }
}
