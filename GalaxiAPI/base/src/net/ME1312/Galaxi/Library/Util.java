package net.ME1312.Galaxi.Library;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

/**
 * Random Utility Class
 */
public final class Util {
    private Util(){}

    /**
     * Checks a value to make sure it's not null
     *
     * @param value Value to check
     * @throws NullPointerException if any are null
     * @return Value
     */
    public static <T> T nullpo(T value) {
        if (value == null) throw new NullPointerException("Illegal null value");
        return value;
    }

    /**
     * Checks values to make sure they're not null
     *
     * @param values Values to check
     * @throws NullPointerException if any are null
     * @return Values
     */
    @SafeVarargs
    public static <T> T[] nullpo(T... values) {
        if (values == null) throw new NullPointerException("Illegal null array");
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null) throw new NullPointerException("Illegal null value at position: [" + i + "]");
        }
        return values;
    }

    /**
     * Checks a value to make sure it's not null
     *
     * @param value Value to check
     * @return If any are null
     */
    public static boolean isNull(Object value) {
        return value == null;
    }

    /**
     * Checks values to make sure they're not null
     *
     * @param values Values to check
     * @return If any are null
     */
    public static boolean isNull(Object... values) {
        if (values == null) return true;
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null) return true;
        }
        return false;
    }

    /**
     * Get keys by value from map
     *
     * @param map Map to search
     * @param value Value to search for
     * @param <K> Key
     * @param <V> Value
     * @return Search results
     */
    public static <K, V> List<K> getBackwards(Map<K, V> map, V value) {
        List<K> values = new ArrayList<K>();

        for (K key : map.keySet()) {
            if (map.get(key) == null || value == null) {
                if (key == value) values.add(key);
            } else if (map.get(key).equals(value)) {
                values.add(key);
            }
        }

        return values;
    }

    /**
     * Get an item from a map ignoring case
     *
     * @param map Map to search
     * @param key Key to search with
     * @param <V> Value
     * @return Search Result
     */
    public static <V> V getCaseInsensitively(Map<String, V> map, String key) {
        HashMap<String, String> insensitivity = new HashMap<String, String>();
        for (String item : map.keySet()) insensitivity.put(item.toLowerCase(), item);
        if (insensitivity.containsKey(key.toLowerCase())) {
            return map.get(insensitivity.get(key.toLowerCase()));
        } else {
            return null;
        }
    }

    /**
     * Gets a new Variable that doesn't match the existing Variables
     *
     * @param existing Existing Variables
     * @param generator Variable Generator
     * @param <V> Variable Type
     * @return Variable
     */
    public static <V> V getNew(Collection<? extends V> existing, Supplier<V> generator) {
        V result = null;
        while (result == null) {
            V tmp = generator.get();
            if (!existing.contains(tmp)) result = tmp;
        }
        return result;
    }

    /**
     * Read Everything from Reader
     *
     * @param rd Reader
     * @return Reader Contents
     * @throws IOException
     */
    public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * Copy a file from the ClassLoader
     *
     * @param loader ClassLoader
     * @param resource Location From
     * @param destination Location To
     */
    public static void copyFromJar(ClassLoader loader, String resource, String destination) {
        File resDestFile = new File(destination);
        try (
                InputStream resStreamIn = loader.getResourceAsStream(resource);
                OutputStream resStreamOut = new FileOutputStream(resDestFile)
        ) {
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = resStreamIn.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get a Field's value using Reflection<br>
     * This method is likely to be removed the moment <a target="_blank" href="https://openjdk.java.net/projects/jigsaw/spec/issues/#module-artifacts">multi-module JPMS jarfiles</a> become available to us
     *
     * @param field Field to grab
     * @param instance Object Instance (Null for static fields)
     * @param <R> Return Type
     * @return Field Value
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <R> R reflect(Field field, Object instance) throws IllegalAccessException {
        field.setAccessible(true);
        try {
            return (R) field.get(instance);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * Set a Field's value using Reflection<br>
     * This method is likely to be removed the moment <a target="_blank" href="https://openjdk.java.net/projects/jigsaw/spec/issues/#module-artifacts">multi-module JPMS jarfiles</a> become available to us
     *
     * @param field Field to write to
     * @param instance Object Instance (Null for static fields)
     * @param value Value to write
     * @throws IllegalAccessException
     */
    public static void reflect(Field field, Object instance, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } finally {
            field.setAccessible(false);
        }
    }

    /**
     * Call a method using Reflection<br>
     * This method is likely to be removed the moment <a target="_blank" href="https://openjdk.java.net/projects/jigsaw/spec/issues/#module-artifacts">multi-module JPMS jarfiles</a> become available to us
     *
     * @param method Method to call
     * @param instance Object Instance (Null for static methods)
     * @param arguments Method Arguments
     * @param <R> Return Type
     * @return Returned Value
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <R> R reflect(Method method, Object instance, Object... arguments) throws InvocationTargetException, IllegalAccessException {
        method.setAccessible(true);
        try {
            return (R) method.invoke(instance, arguments);
        } finally {
            method.setAccessible(false);
        }
    }

    /**
     * Construct an object using Reflection<br>
     * This method is likely to be removed the moment <a target="_blank" href="https://openjdk.java.net/projects/jigsaw/spec/issues/#module-artifacts">multi-module JPMS jarfiles</a> become available to us
     *
     * @param constructor Constructor to use
     * @param arguments Constructor Arguments
     * @param <R> Return Type
     * @return New Instance
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    @SuppressWarnings("unchecked")
    public static <R> R reflect(Constructor<?> constructor, Object... arguments) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        constructor.setAccessible(true);
        try {
            return (R) constructor.newInstance(arguments);
        } finally {
            constructor.setAccessible(false);
        }
    }

    /**
     * Sneak an exception past the compiler
     *
     * @param e Exception
     * @return Nothing &mdash; this method will always throw the given exception
     * @see Try Use <i>Try</i> for sneaky exception handling
     */
    public static RuntimeException sneakyThrow(Throwable e) {
        return Try.<RuntimeException>sneakyThrow(e);
    }

    /**
     * Parse Java escapes in a String
     *
     * @param str String
     * @return Unescaped String
     */
    public static String unescapeJavaString(String str) {
        StringBuilder sb = new StringBuilder(str.length());

        for (int i = 0; i < str.length(); i++) {
            int ch = str.codePointAt(i);
            if (ch == '\\') {
                int nextChar = (i == str.length() - 1) ? '\\' : str
                        .codePointAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    StringBuilder code = new StringBuilder();
                    code.appendCodePoint(nextChar);
                    i++;
                    if ((i < str.length() - 1) && str.codePointAt(i + 1) >= '0'
                            && str.codePointAt(i + 1) <= '7') {
                        code.appendCodePoint(str.codePointAt(i + 1));
                        i++;
                        if ((i < str.length() - 1) && str.codePointAt(i + 1) >= '0'
                                && str.codePointAt(i + 1) <= '7') {
                            code.appendCodePoint(str.codePointAt(i + 1));
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code.toString(), 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode Char: u????
                    // Hex Unicode Codepoint: u{??????}
                    case 'u':
                        try {
                            if (i >= str.length() - 4) throw new IllegalStateException();
                            StringBuilder escape = new StringBuilder();
                            int offset = 2;

                            if (str.codePointAt(i + 2) != '{') {
                                if (i >= str.length() - 5) throw new IllegalStateException();
                                while (offset <= 5) {
                                    Integer.toString(str.codePointAt(i + offset), 16);
                                    escape.appendCodePoint(str.codePointAt(i + offset));
                                    offset++;
                                }
                                offset--;
                            } else {
                                offset++;
                                while (str.codePointAt(i + offset) != '}') {
                                    Integer.toString(str.codePointAt(i + offset), 16);
                                    escape.appendCodePoint(str.codePointAt(i + offset));
                                    offset++;
                                }
                            }
                            sb.append(new String(new int[]{
                                    Integer.parseInt(escape.toString(), 16)
                            }, 0, 1));

                            i += offset;
                            continue;
                        } catch (Throwable e){
                            sb.append('\\');
                            ch = 'u';
                            break;
                        }
                }
                i++;
            }
            sb.appendCodePoint(ch);
        }
        return sb.toString();
    }
}
