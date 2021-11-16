package net.ME1312.Galaxi.Library;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.function.BiFunction;

import static java.lang.invoke.MethodType.methodType;

/**
 * MethodHandle Access Class<br>
 * This class provides a simpler interface for working with Java's MethodHandle API.<br>
 * This is not a replacement for reflection as that's still faster for single requests. Cache these objects when making repeated or frequent requests.
 *
 * @see Util#reflect See <i>Util.reflect()</i> for even simpler access
 * @see MethodHandle See <i>MethodHandle</i> for more advanced functionality
 */
@SuppressWarnings("unchecked")
public final class Access {
    private final Lookup module;
    private Access(Lookup module) {
        this.module = Util.nullpo(module);
    }

    /**
     * Access a resource shared by this module
     */
    public static final Access shared = new Access(MethodHandles.lookup());

    /**
     * Access a resource from another module
     *
     * @param module Module Lookup
     * @return Module Accessor
     */
    public static Access module(Lookup module) {
        return new Access(module);
    }

    /**
     * Access a Class
     *
     * @param clazz Class
     * @return Class Accessor
     */
    public Type type(Class<?> clazz) {
        return new Type(module, clazz);
    }

    /*
     * Base Accessor Class
     */
    private static abstract class Base {
        static final MethodType GENERIC_TYPE = methodType(Object.class);
        static final MethodType VOID_TYPE = methodType(void.class);

        final Lookup search;
        final Class<?> clazz;
        private Base(Lookup search, Class<?> clazz) {
            this.search = search;
            this.clazz = clazz;
        }
    }

    /**
     * Type Accessor Class
     */
    public static final class Type extends Base {
        private static final BiFunction<Class<?>, Lookup, Lookup> ACCESS; static {
            BiFunction<Class<?>, Lookup, Lookup> access;
            try { // Attempt Java 9+ module accessor
                final MethodHandle handle = MethodHandles.publicLookup().findStatic(MethodHandles.class, "privateLookupIn", methodType(Lookup.class, new Class[]{ Class.class, Lookup.class }));
                access = (type, module) -> {
                    try {
                        return (Lookup) handle.invokeExact(type, module);
                    } catch (Throwable e) {
                        throw Util.sneakyThrow(e);
                    }
                };
            } catch (Throwable e) {
                try { // Fallback to Java 8 master accessor
                    final Lookup lookup = Util.reflect(Lookup.class.getDeclaredField("IMPL_LOOKUP"), null);
                    access = (type, module) -> lookup;
                } catch (Throwable x) {
                    throw Util.sneakyThrow(e);
                }
            }
            ACCESS = access;
        }
        private Type(Lookup module, Class<?> clazz) {
            super(ACCESS.apply(clazz, module), clazz);
        }

        /**
         * Access a Constructor
         *
         * @return Constructor Accessor
         */
        public Constructor constructor() {
            return new Constructor(search, clazz);
        }

        /**
         * Access a Method
         *
         * @param name Method Name
         * @return Method Accessor
         */
        public Method method(String name) {
            return new Method(search, clazz, name);
        }

        /**
         * Access a Field
         *
         * @param name Field Name
         * @return Field Accessor
         */
        public Field field(String name) {
            return new Field(search, clazz, name);
        }
    }

    /*
     * MethodHandle API Translator Class
     */
    private static final class Translator<T> {
        private final Try.Function<T, MethodHandle> LOW_LEVEL;
        private final Try.BiFunction<MethodHandle, Class<?>[], MethodHandle> HIGH_LEVEL;
        private Translator(Try.Function<T, MethodHandle> low, Try.BiFunction<MethodHandle, Class<?>[], MethodHandle> high) {
            this.LOW_LEVEL = low;
            this.HIGH_LEVEL = high;
        }
        private Translator(Try.Function<T, MethodHandle> low) {
            this(low, null);
        }
    }

    /**
     * Constructor Accessor Class
     */
    public static final class Constructor extends Base {
        private static final Translator<Constructor> WITHOUT_PARAMETERS = new Translator<>(c -> c.search.findConstructor(c.clazz, VOID_TYPE), (h, t) -> h.asType(GENERIC_TYPE));
        private static final Translator<Constructor> WITH_PARAMETERS = new Translator<>(c -> c.search.findConstructor(c.clazz, methodType(void.class, c.params)).asFixedArity(), (h, t) -> h.asType(methodType(Object.class, t)));

        private MethodHandle handle, invoke;
        private Translator<Constructor> access;
        private Class<?>[] params;
        private Constructor(Lookup search, Class<?> clazz) {
            super(search, clazz);
            this.access = WITHOUT_PARAMETERS;
        }

        /**
         * Define the parameters of this constructor<br>
         * If this method is not called, it is assumed that there are no parameters
         *
         * @param types Parameter Types
         */
        public Constructor parameters(Class<?>... types) {
            if (types.length == 0) {
                this.params = null;
                this.access = WITHOUT_PARAMETERS;
            } else {
                this.params = types;
                this.access = WITH_PARAMETERS;
            }
            return this;
        }

        /**
         * Get the defined parameter types of this constructor
         *
         * @return Parameter Types
         */
        public Class<?>[] parameters() {
            return params;
        }

        /**
         * Finalize and cache the constructor
         */
        public Constructor bind() throws Throwable {
            if (invoke == null) {
                Translator<Constructor> access = this.access;
                MethodHandle handle = this.handle;
                if (handle == null) this.handle = handle = access.LOW_LEVEL.run(this);
                invoke = (access.HIGH_LEVEL == null)? handle : access.HIGH_LEVEL.run(handle, params);
            }
            return this;
        }

        /**
         * Finalize, cache, and select the constructor
         *
         * @return MethodHandle
         */
        public MethodHandle handle() throws Throwable {
            if (handle == null) return handle = access.LOW_LEVEL.run(this);
            return handle;
        }

        /**
         * Finalize, cache, select, and invoke the constructor (with zero arguments)
         *
         * @return Constructed Object
         */
        public <R> R invoke() throws Throwable {
            if (invoke == null) bind();
            return (R) invoke.invokeExact();
        }

        /**
         * Finalize, cache, select, and invoke the constructor
         *
         * @param invocation Invocation Instruction
         * @return Constructed Object
         */
        public <R> R invoke(Try.Function<MethodHandle, Object> invocation) throws Throwable {
            if (invoke == null) bind();
            return (R) invocation.run(invoke);
        }
    }

    /**
     * Method Accessor Class
     */
    public static final class Method extends Base {
        private static final int INSTANCE_FLAG = 1;
        private static final int RETURN_TYPE_FLAG = 2;
        private static final int PARAMETERS_FLAG = 4;
        private static final Translator<Method>[] ACCESS = new Translator[] {
            // static method, void return type, no parameters
            new Translator<Method>(m -> m.search.findStatic(m.clazz, m.name, VOID_TYPE)),
            // instance method, void return type, no parameters
            new Translator<Method>(m -> m.search.findVirtual(m.clazz, m.name, VOID_TYPE).bindTo(m.instance)),
            // static method, object return type, no parameters
            new Translator<Method>(m -> m.search.findStatic(m.clazz, m.name, methodType(m.returns)), (h, t) -> h.asType(GENERIC_TYPE)),
            // instance method, object return type, no parameters
            new Translator<Method>(m -> m.search.findVirtual(m.clazz, m.name, methodType(m.returns)).bindTo(m.instance), (h, t) -> h.asType(GENERIC_TYPE)),
            // static method, void return type, with parameters
            new Translator<Method>(m -> m.search.findStatic(m.clazz, m.name, methodType(void.class, m.params)).asFixedArity()),
            // instance method, void return type, with parameters
            new Translator<Method>(m -> m.search.findVirtual(m.clazz, m.name, methodType(void.class, m.params)).asFixedArity().bindTo(m.instance)),
            // static method, object return type, with parameters
            new Translator<Method>(m -> m.search.findStatic(m.clazz, m.name, methodType(m.returns, m.params)).asFixedArity(), (h, t) -> h.asType(methodType(Object.class, t))),
            // instance method, object return type, with parameters
            new Translator<Method>(m -> m.search.findVirtual(m.clazz, m.name, methodType(m.returns, m.params)).asFixedArity().bindTo(m.instance), (h, t) -> h.asType(methodType(Object.class, t))),
        };
        private int type = 0;
        private MethodHandle handle, invoke;
        private final String name;
        private Object instance;
        private Class<?> returns;
        private Class<?>[] params;
        private Method(Lookup search, Class<?> clazz, String name) {
            super(search, clazz);
            this.name = name;
        }

        /**
         * Switch to instance method selection mode<br>
         * If this method is not called, we remain in static method selection mode
         *
         * @param instance Instance
         */
        public Method instance(Object instance) {
            if (instance == null) {
                this.instance = null;
                this.type &= ~INSTANCE_FLAG;
            } else {
                this.instance = instance;
                this.type |= INSTANCE_FLAG;
            }
            return this;
        }

        /**
         * Get the instance of this method
         *
         * @return Instance
         */
        public Object instance() {
            return instance;
        }

        /**
         * Define the return type of this method<br>
         * If this method is not called, the return type is assumed to be <i>void</i>
         *
         * @param type Return Type
         */
        public Method returns(Class<?> type) {
            if (type == null) {
                this.returns = null;
                this.type &= ~RETURN_TYPE_FLAG;
            } else {
                this.returns = type;
                this.type |= RETURN_TYPE_FLAG;
            }
            return this;
        }

        /**
         * Get the defined return type of this method
         *
         * @return Return Type
         */
        public Class<?> returns() {
            return returns;
        }

        /**
         * Define the parameters of this method<br>
         * If this method is not called, it is assumed that there are no parameters
         *
         * @param types Parameter Types
         */
        public Method parameters(Class<?>... types) {
            if (types.length == 0) {
                this.params = null;
                this.type &= ~PARAMETERS_FLAG;
            } else {
                this.params = types;
                this.type |= PARAMETERS_FLAG;
            }
            return this;
        }

        /**
         * Get the defined parameter types of this method
         *
         * @return Parameter Types
         */
        public Class<?>[] parameters() {
            return params;
        }

        /**
         * Finalize and cache the method
         */
        public Method bind() throws Throwable {
            if (invoke == null) {
                Translator<Method> access = ACCESS[type];
                MethodHandle handle = this.handle;
                if (handle == null) this.handle = handle = access.LOW_LEVEL.run(this);
                invoke = (access.HIGH_LEVEL == null)? handle : access.HIGH_LEVEL.run(handle, params);
            }
            return this;
        }

        /**
         * Finalize, cache, and select the method
         *
         * @return MethodHandle
         */
        public MethodHandle handle() throws Throwable {
            if (handle == null) return handle = ACCESS[type].LOW_LEVEL.run(this);
            return handle;
        }

        /**
         * Finalize, cache, select, and invoke the method (with zero arguments)
         *
         * @return Method Return Value
         */
        public <R> R invoke() throws Throwable {
            if (invoke == null) bind();
            return (R) invoke.invokeExact();
        }

        /**
         * Finalize, cache, select, and invoke the method
         *
         * @param invocation Invocation Instruction
         * @return Method Return Value
         */
        public <R> R invoke(Try.Function<MethodHandle, Object> invocation) throws Throwable {
            if (invoke == null) bind();
            return (R) invocation.run(invoke);
        }
    }

    /**
     * Field Accessor Class
     */
    public static final class Field extends Base {
        private static final Translator<Field>[] STATIC = new Translator[] {
            new Translator<Field>(f -> f.search.findStaticGetter(f.clazz, f.name, f.value), (h, t) -> h.asType(GENERIC_TYPE)),
            new Translator<Field>(f -> f.search.findStaticSetter(f.clazz, f.name, f.value).asFixedArity()),
        };
        private static final Translator<Field>[] INSTANCE = new Translator[] {
            new Translator<Field>(f -> f.search.findGetter(f.clazz, f.name, f.value).bindTo(f.instance), (h, t) -> h.asType(GENERIC_TYPE)),
            new Translator<Field>(f -> f.search.findSetter(f.clazz, f.name, f.value).asFixedArity().bindTo(f.instance)),
        };
        private MethodHandle setter, getter, invoke;
        private Translator<Field>[] access;
        private final String name;
        private Object instance;
        private Class<?> value;
        private Field(Lookup search, Class<?> clazz, String name) {
            super(search, clazz);
            this.name = name;
            this.access = STATIC;
        }

        /**
         * Switch to instance field selection mode<br>
         * If this method is not called, we remain in static field selection mode
         *
         * @param instance Instance
         */
        public Field instance(Object instance) {
            if (instance == null) {
                this.instance = null;
                this.access = STATIC;
            } else {
                this.instance = instance;
                this.access = INSTANCE;
            }
            return this;
        }

        /**
         * Get the instance of this field
         *
         * @return Instance
         */
        public Object instance() {
            return instance;
        }

        /**
         * Define the value type of this field<br>
         * Calling this method is required to successfully select a field
         *
         * @param type Value Type
         */
        public Field value(Class<?> type) {
            this.value = type;
            return this;
        }

        /**
         * Get the defined value type of this field
         *
         * @return Value Type
         */
        public Class<?> value() {
            return value;
        }

        /**
         * Finalize and cache the field getter<br>
         * @see #setter Use <i>setter()</i> to do the same to the field setter
         */
        public Field bind() throws Throwable {
            if (invoke == null) {
                Translator<Field> accessor = this.access[0];
                MethodHandle getter = this.getter;
                if (getter == null) this.getter = getter = accessor.LOW_LEVEL.run(this);
                invoke = (accessor.HIGH_LEVEL == null)? getter : accessor.HIGH_LEVEL.run(getter, null);
            }
            return this;
        }

        /**
         * Finalize, cache, and select the field setter
         *
         * @return MethodHandle
         */
        public MethodHandle setter() throws Throwable {
            if (setter == null) return setter = access[1].LOW_LEVEL.run(this);
            return setter;
        }

        /**
         * Finalize, cache, and select the field getter
         *
         * @return MethodHandle
         */
        public MethodHandle getter() throws Throwable {
            if (getter == null) return getter = access[0].LOW_LEVEL.run(this);
            return getter;
        }

        /**
         * Finalize, cache, select, and invoke the field getter
         *
         * @return Field Value
         */
        public <R> R get() throws Throwable {
            if (invoke == null) bind();
            return (R) invoke.invokeExact();
        }

        /**
         * Finalize, cache, select, and invoke the field getter
         *
         * @param invocation Invocation Instruction
         * @return Field Value
         */
        public <R> R get(Try.Function<MethodHandle, Object> invocation) throws Throwable {
            if (invoke == null) bind();
            return (R) invocation.run(invoke);
        }
    }
}
