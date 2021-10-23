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
     * Checks values to make sure they're not null
     *
     * @param values Values to check
     * @return If any are null
     */
    public static boolean isNull(Object... values) {
        for (Object value : values) {
            if (value == null) {
                return true;
            }
        }
        return false;
    }


    /**
     * Checks values to make sure they're not null
     *
     * @param value Values to check
     * @throws NullPointerException if any are null
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
     */
    public static void nullpo(Object... values) {
        for (int i = 0; i < values.length; ++i) {
            if (values[i] == null) throw new NullPointerException("Illegal null value at position: [" + i + "]");
        }
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
     * Copy from the Class Loader
     *
     * @param loader ClassLoader
     * @param resource Location From
     * @param destination Location To
     */
    public static void copyFromJar(ClassLoader loader, String resource, String destination) {
        InputStream resStreamIn = loader.getResourceAsStream(resource);
        File resDestFile = new File(destination);
        try {
            OutputStream resStreamOut = new FileOutputStream(resDestFile);
            int readBytes;
            byte[] buffer = new byte[4096];
            while ((readBytes = resStreamIn.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
            resStreamOut.close();
            resStreamIn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get a Field's value using Reflection
     *
     * @param field Field to grab
     * @param instance Object Instance (Null for static fields)
     * @param <R> Return Type
     * @return Field Value
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <R> R reflect(Field field, Object instance) throws IllegalAccessException {
        R value;
        field.setAccessible(true);
        value = (R) field.get(instance);
        field.setAccessible(false);
        return value;
    }

    /**
     * Set a Field's value using Reflection
     *
     * @param field Field to write to
     * @param instance Object Instance (Null for static fields)
     * @param value Value to write
     * @throws IllegalAccessException
     */
    public static void reflect(Field field, Object instance, Object value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(instance, value);
        field.setAccessible(false);
    }

    /**
     * Call a method using Reflection
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
        R value;
        method.setAccessible(true);
        value = (R) method.invoke(instance, arguments);
        method.setAccessible(false);
        return value;
    }

    /**
     * Construct an object using Reflection
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
        R value;
        constructor.setAccessible(true);
        value = (R) constructor.newInstance(arguments);
        constructor.setAccessible(false);
        return value;
    }

    /**
     * Parse escapes in a Java String
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
