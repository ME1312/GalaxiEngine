package net.ME1312.Galaxi.Library.Exception;

/**
 * Invalid Server Exception
 */
public class InvalidServerException extends IllegalStateException {
    public InvalidServerException() {}
    public InvalidServerException(String s) {
        super(s);
    }
}
