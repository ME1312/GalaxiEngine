package net.ME1312.Galaxi.Library.Exception;

/**
 * Illegal String Value Exception
 */
public class IllegalStringValueException extends IllegalArgumentException {
    public IllegalStringValueException() {}
    public IllegalStringValueException(String s) {
        super(s);
    }
}
