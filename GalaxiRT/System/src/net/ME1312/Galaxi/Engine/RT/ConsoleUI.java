package net.ME1312.Galaxi.Engine.RT;

import net.ME1312.Galaxi.Log.LogMessenger;

public interface ConsoleUI extends LogMessenger {
    void open();
    void close();
    void destroy();
}
