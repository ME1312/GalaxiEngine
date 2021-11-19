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
public class Access {
    private static final BiFunction<java.lang.Class<?>, Lookup, Lookup> ACCESS; static {
        BiFunction<java.lang.Class<?>, Lookup, Lookup> access;
        try { // Attempt Java 9+ module accessor
            AccessJ9.init();
            access = (type, module) -> {
                try {
                    return AccessJ9.nonPublic(type, module);
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
    final Lookup module;
    private Access(Lookup module) {
        this.module = Util.nullpo(module);
    }

    /**
     * Access a resource that has been shared to all modules; a public resource
     */
    public static final Access shared = new Access(MethodHandles.publicLookup()) {
        @Override
        public Class type(java.lang.Class<?> clazz) {
            return new Class(module, clazz);
        }
    };

    /**
     * Access a resource from the unnamed module; the same module as Galaxi
     */
    public static final Access unnamed = new Access(MethodHandles.lookup());

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
    public Class type(java.lang.Class<?> clazz) {
        return new Class(ACCESS.apply(clazz, module), clazz);
    }

    /**
     * Type Accessor Class
     */
    public static final class Class extends Base {
        private Class(Lookup search, java.lang.Class<?> clazz) {
            super(search, clazz);
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
         * Access a Method
         *
         * @param rtype Method Return Type
         * @param name Method Name
         * @return Method Accessor
         */
        public Method method(java.lang.Class<?> rtype, String name) {
            return new Method(search, clazz, name, rtype);
        }

        /**
         * Access a Field
         *
         * @param ftype Field Type
         * @param name Field Name
         * @return Field Accessor
         */
        public Field field(java.lang.Class<?> ftype, String name) {
            return new Field(search, clazz, name, ftype);
        }
    }

    /*
     * MethodHandle API Translator Class
     */
    private static final class Translator<T> {
        private final Try.Function<T, MethodHandle> LOW_LEVEL;
        private final Try.BiFunction<MethodHandle, java.lang.Class<?>[], MethodHandle> HIGH_LEVEL;
        private Translator(Try.Function<T, MethodHandle> low, Try.BiFunction<MethodHandle, java.lang.Class<?>[], MethodHandle> high) {
            this.LOW_LEVEL = low;
            this.HIGH_LEVEL = high;
        }
        private Translator(Try.Function<T, MethodHandle> low) {
            this(low, null);
        }
    }

    /*
     * Base Accessor Class
     */
    private static abstract class Base {
        static final MethodType GENERIC_TYPE = methodType(Object.class);
        static final MethodType VOID_TYPE = methodType(void.class);

        final Lookup search;
        final java.lang.Class<?> clazz;
        private Base(Lookup search, java.lang.Class<?> clazz) {
            this.search = search;
            this.clazz = clazz;
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
        private java.lang.Class<?>[] params;
        private Constructor(Lookup search, java.lang.Class<?> clazz) {
            super(search, clazz);
            this.access = WITHOUT_PARAMETERS;
        }

        /**
         * Create an editable clone of this accessor
         *
         * @return Cloned Constructor Accessor
         */
        @Override
        public Constructor clone() {
            Constructor c = new Constructor(search, clazz);
            c.access = access;
            c.params = params;
            return c;
        }

        /**
         * Define the parameters of this constructor<br>
         * If this method is not called, it is assumed that there are no parameters
         *
         * @param types Parameter Types
         */
        public Constructor parameters(java.lang.Class<?>... types) {
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
         * Get the parameter types of this constructor
         *
         * @return Parameter Types
         */
        public java.lang.Class<?>[] parameters() {
            return (params == null)? null : params.clone();
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
        private java.lang.Class<?> returns;
        private java.lang.Class<?>[] params;
        private Method(Lookup search, java.lang.Class<?> clazz, String name) {
            super(search, clazz);
            this.name = name;
        }
        private Method(Lookup search, java.lang.Class<?> clazz, String name, java.lang.Class<?> type) {
            super(search, clazz);
            this.name = name;
            if (type != null && type != void.class) {
                this.returns = type;
                this.type = RETURN_TYPE_FLAG;
            }
        }

        /**
         * Create an editable clone of this accessor
         *
         * @return Cloned Method Accessor
         */
        @Override
        public Method clone() {
            Method m = new Method(search, clazz, name);
            m.type = type;
            m.instance = instance;
            m.returns = returns;
            m.params = params;
            return m;
        }

        /**
         * Get the name of this method
         *
         * @return Method Name
         */
        public String name() {
            return name;
        }

        /**
         * Define the return type of this method<br>
         * If this method is not called, the return type is assumed to be <i>void</i>
         *
         * @param type Return Type
         */
        public Method returns(java.lang.Class<?> type) {
            if (type == null || type == void.class) {
                this.returns = null;
                this.type &= ~RETURN_TYPE_FLAG;
            } else {
                this.returns = type;
                this.type |= RETURN_TYPE_FLAG;
            }
            return this;
        }

        /**
         * Get the return type of this method
         *
         * @return Return Type
         */
        public java.lang.Class<?> returns() {
            return returns;
        }

        /**
         * Define the parameters of this method<br>
         * If this method is not called, it is assumed that there are no parameters
         *
         * @param types Parameter Types
         */
        public Method parameters(java.lang.Class<?>... types) {
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
         * Get the parameter types of this method
         *
         * @return Parameter Types
         */
        public java.lang.Class<?>[] parameters() {
            return (params == null)? null : params.clone();
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
         * Get the instance this method will run under
         *
         * @return Instance
         */
        public Object instance() {
            return instance;
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
            new Translator<Field>(f -> f.search.findStaticGetter(f.clazz, f.name, f.type), (h, t) -> h.asType(GENERIC_TYPE)),
            new Translator<Field>(f -> f.search.findStaticSetter(f.clazz, f.name, f.type).asFixedArity()),
        };
        private static final Translator<Field>[] INSTANCE = new Translator[] {
            new Translator<Field>(f -> f.search.findGetter(f.clazz, f.name, f.type).bindTo(f.instance), (h, t) -> h.asType(GENERIC_TYPE)),
            new Translator<Field>(f -> f.search.findSetter(f.clazz, f.name, f.type).asFixedArity().bindTo(f.instance)),
        };
        private MethodHandle setter, getter, invoke;
        private Translator<Field>[] access;
        private final String name;
        private final java.lang.Class<?> type;
        private Object instance;
        private Field(Lookup search, java.lang.Class<?> clazz, String name, java.lang.Class<?> type) {
            super(search, clazz);
            this.name = name;
            this.type = type;
            this.access = STATIC;
        }

        /**
         * Get the name of this field
         *
         * @return Field Name
         */
        public String name() {
            return name;
        }

        /**
         * Get the data type of this field
         *
         * @return Field Data Type
         */
        public java.lang.Class<?> type() {
            return type;
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
         * Get the instance this field belongs to
         *
         * @return Instance
         */
        public Object instance() {
            return instance;
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
