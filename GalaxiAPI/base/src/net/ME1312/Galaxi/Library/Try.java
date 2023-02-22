package net.ME1312.Galaxi.Library;

/**
 * Exception Handler Class<br>
 * Remember that use of this class should always make your code shorter &mdash; it is not a replacement for native try-catch blocks
 */
public final class Try {
    private final java.util.function.Consumer<Throwable> sup;

    /**
     * Handle all exceptions
     */
    public static final Try all = new Try(Try::suppress);

    /**
     * Handle no exceptions
     * @see Util#sneakyThrow(Throwable) Use <i>Util.sneakyThrow()</i> to sneaky throw exceptions directly
     */
    public static final Try none = new Try(Try::sneakyThrow);

    /**
     * Handle specific exceptions
     *
     * @param exceptions Exception types to handle
     * @return Exception Handler
     */
    @SafeVarargs
    public static Try expect(Class<? extends Throwable>... exceptions) {
        return new Try(Util.nullpo(exceptions));
    }
    private Try(Class<? extends Throwable>[] $) {
        this.sup = new java.util.function.Consumer<Throwable>() {
            public void accept(Throwable e) {
                for (Class<? extends Throwable> t : $) {
                    if (t.isInstance(e)) {
                        return;
                    }
                }
                Try.sneakyThrow(e);
            }
        };
    }

    /**
     * Handle specific exceptions
     *
     * @param suppressor Exception suppressor
     */
    public Try(java.util.function.Consumer<Throwable> suppressor) {
        this.sup = Util.nullpo(suppressor);
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
            sup.accept(e);
            return false;
        }
    }

    /**
     * Run some code
     *
     * @param code Code to run
     * @param err Code to run for handling exceptions
     * @return true if the code was run successfully (without exceptions)
     */
    public boolean run(Runnable code, java.util.function.Consumer<Throwable> err) {
        Util.nullpo(err);
        try {
            code.run();
            return true;
        } catch (Throwable e) {
            sup.accept(e);
            err.accept(e);
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
            sup.accept(e);
            return sneakyNull();
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
            sup.accept(e);
            return def;
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param err Code to run for handling exceptions
     * @param def Default value
     * @return The return value of that code (or def in the event of an exception)
     */
    public <T> T get(Supplier<T> value, java.util.function.Consumer<Throwable> err, T def) {
        Util.nullpo(err);
        try {
            return value.run();
        } catch (Throwable e) {
            sup.accept(e);
            err.accept(e);
            return def;
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param err Code to run for handling exceptions
     * @return The return value of that code (or null in the event of an exception)
     */
    public <T> T getOrConsume(Supplier<T> value, java.util.function.Consumer<Throwable> err) {
        Util.nullpo(err);
        try {
            return value.run();
        } catch (Throwable e) {
            sup.accept(e);
            err.accept(e);
            return sneakyNull();
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
            sup.accept(e);
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
            sup.accept(e);
            return def.apply(e);
        }
    }

    /**
     * Get a value despite exceptions
     *
     * @param value Code to run
     * @param err Code to run for handling exceptions
     * @param def Code to run for generating the default value in the event of an exception
     * @return The return value of either code block
     */
    public <T> T getOrFunction(Supplier<T> value, java.util.function.Consumer<Throwable> err, java.util.function.Supplier<? extends T> def) {
        Util.nullpo(err, def);
        try {
            return value.run();
        } catch (Throwable e) {
            sup.accept(e);
            err.accept(e);
            return def.get();
        }
    }

    /**
     * Suppresses all exceptions
     *
     * @param e Exception
     */
    @SuppressWarnings("UnnecessaryReturnStatement")
    private static void suppress(Throwable e) {
        return;
    }

    /**
     * Re-throws all exceptions
     *
     * @param e Exception
     * @throws T The supplied Exception
     */
    @SuppressWarnings("unchecked")
    static <T extends Throwable> T sneakyThrow(Throwable e) throws T {
        throw (T) e;
    }

    /**
     * Supplies a null value in the event of an expected exception
     *
     * @return <i>null</i>
     */
    private <T> T sneakyNull() {
        return null;
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
     * BiConsumer that could throw an exception
     * @see java.util.function.BiConsumer
     */
    public interface BiConsumer<T1, T2> {
        void run(T1 arg1, T2 arg2) throws Throwable;
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

    /**
     * BiFunction that could throw an exception
     * @see java.util.function.BiFunction
     */
    public interface BiFunction<T1, T2, R> {
        R run(T1 arg1, T2 arg2) throws Throwable;
    }
}
