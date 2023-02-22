package net.ME1312.Galaxi.Engine.Runtime;

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

                console.jline.printAbove(buffer.append("\u001B[m").toString());
                console.jline.getTerminal().flush();
            } else {
                buffer.append(s);
            }
        } catch (Exception e) {}
    }
}
