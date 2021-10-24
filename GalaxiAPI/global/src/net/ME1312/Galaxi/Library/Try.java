package net.ME1312.Galaxi.Library;

/**
 * Exception Handler Class<br>
 * Remember that use of this class should always make your code shorter &mdash; it is not a replacement for native try-catch blocks
 */
public final class Try {
    private final Class<? extends Throwable>[] types;
    private final boolean suppress;

    /**
     * Handle all exceptions
     */
    public static final Try all = new Try();

    @SuppressWarnings("unchecked")
    private Try() {
        this.types = new Class[0];
        this.suppress = true;
    }

    /**
     * Handle specific exceptions
     *
     * @param exceptions Exception types to handle
     * @return Exception Handler
     */
    @SafeVarargs
    public static Try handle(Class<? extends Throwable>... exceptions) {
        return new Try(exceptions);
    }

    private Try(Class<? extends Throwable>[] exceptions) {
        Util.nullpo((Object) exceptions);
        this.types = exceptions;
        this.suppress = false;
    }

    /**
     * Run some code
     *
     * @param code Code to run
     * @return true if the code was run successfully (without exceptions)
     */
    public boolean run(Runnable code) {
        try {
            code.run();
            return true;
        } catch (Throwable e) {
            suppress(e);
            return false;
        }
    }

    /**
     * Run some code
     *
     * @param code Code to run
     * @param fallback Code to run for handling exceptions
     * @return true if the code was run successfully (without exceptions)
     */
    public boolean run(Runnable code, java.util.function.Consumer<Throwable> fallback) {
        Util.nullpo(fallback);
        try {
            code.run();
            return true;
        } catch (Throwable e) {
            suppress(e);
            fallback.accept(e);
            return false;
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @return The return value of that code (or null in the event of an exception)
     */
    public <T> T get(Supplier<T> value) {
        try {
            return value.run();
        } catch (Throwable e) {
            suppress(e);
            return null;
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param def Default value
     * @return The return value of that code (or def in the event of an exception)
     */
    public <T> T get(Supplier<T> value, T def) {
        try {
            return value.run();
        } catch (Throwable e) {
            suppress(e);
            return def;
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param fallback Code to run for handling exceptions
     * @param def Default value
     * @return The return value of that code (or def in the event of an exception)
     */
    public <T> T get(Supplier<T> value, java.util.function.Consumer<Throwable> fallback, T def) {
        Util.nullpo(fallback);
        try {
            return value.run();
        } catch (Throwable e) {
            suppress(e);
            fallback.accept(e);
            return def;
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param fallback Code to run for handling exceptions
     * @return The return value of that code (or null in the event of an exception)
     */
    public <T> T getOrConsume(Supplier<T> value, java.util.function.Consumer<Throwable> fallback) {
        Util.nullpo(fallback);
        try {
            return value.run();
        } catch (Throwable e) {
            suppress(e);
            fallback.accept(e);
            return null;
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param def Code to run for generating the default value in the event of an exception
     * @return The return value of either code block
     */
    public <T> T getOrSupply(Supplier<T> value, java.util.function.Supplier<? extends T> def) {
        Util.nullpo(def);
        try {
            return value.run();
        } catch (Throwable e) {
            suppress(e);
            return def.get();
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param def Code to run for handling exceptions and/or generating the default value in the event of an exception
     * @return The return value of either code block
     */
    public <T> T getOrFunction(Supplier<T> value, java.util.function.Function<Throwable, ? extends T> def) {
        Util.nullpo(def);
        try {
            return value.run();
        } catch (Throwable e) {
            suppress(e);
            return def.apply(e);
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param fallback Code to run for handling exceptions
     * @param def Code to run for generating the default value in the event of an exception
     * @return The return value of either code block
     */
    public <T> T getOrFunction(Supplier<T> value, java.util.function.Consumer<Throwable> fallback, java.util.function.Supplier<? extends T> def) {
        Util.nullpo(fallback, def);
        try {
            return value.run();
        } catch (Throwable e) {
            suppress(e);
            fallback.accept(e);
            return def.get();
        }
    }

    /**
     * Suppresses an exception that we're handling
     *
     * @param e Exception
     */
    private void suppress(Throwable e) {
        for (Class<? extends Throwable> t : types) {
            if (t.isInstance(e)) {
                return;
            }
        }
        if (suppress) return;
        throw throwAnyway(e);
    }

    private static RuntimeException throwAnyway(Throwable e) {
        return Try.<RuntimeException>sneakyThrow(e);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> T sneakyThrow(Throwable e) throws T {
        throw (T) e;
    }

    /**
     * Runnable that could throw an exception
     * @see java.lang.Runnable
     */
    public interface Runnable {
        void run() throws Throwable;
    }

    /**
     * Consumer that could throw an exception
     * @see java.util.function.Consumer
     */
    public interface Consumer<T> {
        void run(T arg) throws Throwable;
    }

    /**
     * Supplier that could throw an exception
     * @see java.util.function.Supplier
     */
    public interface Supplier<R> {
        R run() throws Throwable;
    }

    /**
     * Function that could throw an exception
     * @see java.util.function.Function
     */
    public interface Function<T, R> {
        R run(T arg) throws Throwable;
    }
}
