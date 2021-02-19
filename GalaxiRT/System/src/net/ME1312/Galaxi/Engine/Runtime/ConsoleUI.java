package net.ME1312.Galaxi.Engine.Runtime;

import net.ME1312.Galaxi.Log.LogMessenger;

interface ConsoleUI extends LogMessenger {
    void open();
    void close();
    void destroy();
}
