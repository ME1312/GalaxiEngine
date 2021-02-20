package net.ME1312.Galaxi.Engine.Runtime;

import java.io.IOException;

interface ConsoleUI {
    void open();
    void log(String message) throws IOException;
    void close();
    void destroy();
}
