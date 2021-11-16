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
    private static final MethodType GENERIC_TYPE = methodType(Object.class);
    private static final MethodType VOID_TYPE = methodType(void.class);
    private static final BiFunction<Class<?>, Lookup, Lookup> ACCESS;
    static {
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
     * Access a Constructor
     *
     * @param clazz Containing Class
     * @return Constructor Accessor
     */
    public Constructor constructor(Class<?> clazz) {
        return new Constructor(ACCESS.apply(clazz, module), clazz);
    }

    /**
     * Access a Method
     *
     * @param clazz Containing Class
     * @param name Method Name
     * @return Method Accessor
     */
    public Method method(Class<?> clazz, String name) {
        return new Method(ACCESS.apply(clazz, module), clazz, name);
    }

    /**
     * Access a Field
     *
     * @param clazz Containing Class
     * @param name Field Name
     * @return Field Accessor
     */
    public Field field(Class<?> clazz, String name) {
        return new Field(ACCESS.apply(clazz, module), clazz, name);
    }

    /*
     * Cached MethodHandle Constructor Class
     */
    private static final class Cached<T> {
        private final Try.Function<T, MethodHandle> constructor;
        private final Try.BiFunction<MethodHandle, Class<?>[], MethodHandle> invocation;
        private Cached(Try.Function<T, MethodHandle> constructor, Try.BiFunction<MethodHandle, Class<?>[], MethodHandle> invocation) {
            this.constructor = constructor;
            this.invocation = invocation;
        }
        private Cached(Try.Function<T, MethodHandle> constructor) {
            this(constructor, null);
        }
    }

    /**
     * Constructor Accessor Class
     */
    public static final class Constructor {
        private static final Cached<Constructor> WITHOUT_PARAMETERS = new Cached<>(c -> c.search.findConstructor(c.clazz, VOID_TYPE), (h, t) -> h.asType(GENERIC_TYPE));
        private static final Cached<Constructor> WITH_PARAMETERS = new Cached<>(c -> c.search.findConstructor(c.clazz, methodType(void.class, c.params)).asFixedArity(), (h, t) -> h.asType(methodType(Object.class, t)));

        private MethodHandle handle, invoke;
        private Cached<Constructor> accessor;
        private final Lookup search;
        private final Class<?> clazz;
        private Class<?>[] params;
        private Constructor(Lookup search, Class<?> clazz) {
            this.search = search;
            this.clazz = clazz;
            this.accessor = WITHOUT_PARAMETERS;
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
                this.accessor = WITHOUT_PARAMETERS;
            } else {
                this.params = types;
                this.accessor = WITH_PARAMETERS;
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
                Cached<Constructor> accessor = this.accessor;
                MethodHandle handle = this.handle;
                if (handle == null) this.handle = handle = accessor.constructor.run(this);
                invoke = (accessor.invocation == null)? handle : accessor.invocation.run(handle, params);
            }
            return this;
        }

        /**
         * Finalize, cache, and select the constructor
         *
         * @return MethodHandle
         */
        public MethodHandle handle() throws Throwable {
            if (handle == null) return handle = accessor.constructor.run(this);
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
    public static final class Method {
        private static final int INSTANCE_FLAG = 1;
        private static final int RETURN_TYPE_FLAG = 2;
        private static final int PARAMETERS_FLAG = 4;
        private static final Cached<Method>[] ACCESSORS = new Cached[] {
            // static method, void return type, no parameters
            new Cached<Method>(m -> m.search.findStatic(m.clazz, m.name, VOID_TYPE)),
            // instance method, void return type, no parameters
            new Cached<Method>(m -> m.search.findVirtual(m.clazz, m.name, VOID_TYPE).bindTo(m.instance)),
            // static method, object return type, no parameters
            new Cached<Method>(m -> m.search.findStatic(m.clazz, m.name, methodType(m.returns)), (h, t) -> h.asType(GENERIC_TYPE)),
            // instance method, object return type, no parameters
            new Cached<Method>(m -> m.search.findVirtual(m.clazz, m.name, methodType(m.returns)).bindTo(m.instance), (h, t) -> h.asType(GENERIC_TYPE)),
            // static method, void return type, with parameters
            new Cached<Method>(m -> m.search.findStatic(m.clazz, m.name, methodType(void.class, m.params)).asFixedArity()),
            // instance method, void return type, with parameters
            new Cached<Method>(m -> m.search.findVirtual(m.clazz, m.name, methodType(void.class, m.params)).asFixedArity().bindTo(m.instance)),
            // static method, object return type, with parameters
            new Cached<Method>(m -> m.search.findStatic(m.clazz, m.name, methodType(m.returns, m.params)).asFixedArity(), (h, t) -> h.asType(methodType(Object.class, t))),
            // instance method, object return type, with parameters
            new Cached<Method>(m -> m.search.findVirtual(m.clazz, m.name, methodType(m.returns, m.params)).asFixedArity().bindTo(m.instance), (h, t) -> h.asType(methodType(Object.class, t))),
        };
        private int type = 0;
        private MethodHandle handle, invoke;
        private final Lookup search;
        private final Class<?> clazz;
        private final String name;
        private Object instance;
        private Class<?> returns;
        private Class<?>[] params;
        private Method(Lookup search, Class<?> clazz, String name) {
            this.search = search;
            this.clazz = clazz;
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
                Cached<Method> accessor = ACCESSORS[type];
                MethodHandle handle = this.handle;
                if (handle == null) this.handle = handle = accessor.constructor.run(this);
                invoke = (accessor.invocation == null)? handle : accessor.invocation.run(handle, params);
            }
            return this;
        }

        /**
         * Finalize, cache, and select the method
         *
         * @return MethodHandle
         */
        public MethodHandle handle() throws Throwable {
            if (handle == null) return handle = ACCESSORS[type].constructor.run(this);
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
    public static final class Field {
        private static final Cached<Field>[] STATIC = new Cached[] {
            new Cached<Field>(f -> f.search.findStaticGetter(f.clazz, f.name, f.type), (h, t) -> h.asType(GENERIC_TYPE)),
            new Cached<Field>(f -> f.search.findStaticSetter(f.clazz, f.name, f.type).asFixedArity()),
        };
        private static final Cached<Field>[] INSTANCE = new Cached[] {
            new Cached<Field>(f -> f.search.findGetter(f.clazz, f.name, f.type).bindTo(f.instance), (h, t) -> h.asType(GENERIC_TYPE)),
            new Cached<Field>(f -> f.search.findSetter(f.clazz, f.name, f.type).asFixedArity().bindTo(f.instance)),
        };
        private MethodHandle setter, getter, invoke;
        private Cached<Field>[] accessor;
        private final Lookup search;
        private final Class<?> clazz;
        private final String name;
        private Object instance;
        private Class<?> type;
        private Field(Lookup search, Class<?> clazz, String name) {
            this.search = search;
            this.clazz = clazz;
            this.name = name;
            this.accessor = STATIC;
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
                this.accessor = STATIC;
            } else {
                this.instance = instance;
                this.accessor = INSTANCE;
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
         * Define the data type of this field<br>
         * Calling this method is required to successfully select a field
         *
         * @param type Field Type
         */
        public Field type(Class<?> type) {
            this.type = type;
            return this;
        }

        /**
         * Get the defined data type of this field
         *
         * @return Field Type
         */
        public Class<?> type() {
            return type;
        }

        /**
         * Finalize and cache the field getter<br>
         * @see #setter Use <i>setter()</i> to do the same to the field setter
         */
        public Field bind() throws Throwable {
            if (invoke == null) {
                Cached<Field> accessor = this.accessor[0];
                MethodHandle getter = this.getter;
                if (getter == null) this.getter = getter = accessor.constructor.run(this);
                invoke = (accessor.invocation == null)? getter : accessor.invocation.run(getter, null);
            }
            return this;
        }

        /**
         * Finalize, cache, and select the field setter
         *
         * @return MethodHandle
         */
        public MethodHandle setter() throws Throwable {
            if (setter == null) return setter = accessor[1].constructor.run(this);
            return setter;
        }

        /**
         * Finalize, cache, and select the field getter
         *
         * @return MethodHandle
         */
        public MethodHandle getter() throws Throwable {
            if (getter == null) return getter = accessor[0].constructor.run(this);
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
