package net.ME1312.Galaxi.Library.Log;

import java.io.IOException;

/**
 * String Transfer Class
 */
public abstract class StringOutputStream {

    /**
     * Recieve a String
     *
     * @param s String
     */
    public abstract void write(String s) throws IOException;
}
