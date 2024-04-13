package me.george.testing.utils;

import me.george.testing.Testing;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static utils for using reflection.
 * Created by George K. on 10/22/2023.
 */
@SuppressWarnings({"unused"})
public class ReflectionUtil {

    private static final Map<Class<?>, Class<?>> PRIMITIVES = new HashMap<>();
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<Class<?>, Class<?>> REPLACE = new HashMap<>();
    private static final Map<String, Class<?>> classCacheMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Map<String, Field>> fieldCacheMap = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Constructor<?>> emptyConstructorCache = new ConcurrentHashMap<>();

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /**
     * Execute a method of a given class and object
     *
     * @param obj        The object to execute the method on.
     * @param type       The class which owns the method.
     * @param methodName The method name to run.
     * @param args       The arguments to run the method with.
     * @return result
     */
    public static Object exec(Object obj, Class<?> type, String methodName, Object... args) {
        Class<?>[] argTypes = getClasses(true, args); // Generate a list of the classes used to get the method.

        try {
            Method m = type.getDeclaredMethod(methodName, argTypes);
            m.setAccessible(true);
            return m.invoke(obj, args);
        } catch (NoSuchMethodException name) {
            try {
                Method foundMethod = null;
                for (Method method : type.getDeclaredMethods()) {
                    if (method.getName().equals(methodName) && method.getParameterCount() == (args != null ? args.length : 0)) {
                        if (foundMethod != null) {
                            Testing.warn("Found multiple methods named '%s' when trying to execute one via reflection.", methodName);
                            return null;
                        } else {
                            foundMethod = method;
                        }
                    }
                }

                if (foundMethod == null) {
                    Testing.warn("Couldn't find method '%s' to execute via reflection!", methodName);
                    return null;
                }

                foundMethod.setAccessible(true);
                return foundMethod.invoke(obj, args);
            } catch (Throwable th2) {
                Testing.handleError(th2, "Error executing method '%s' via reflection!", methodName);
            }
        } catch (Throwable th) {
            Testing.handleError(th, "Error executing method '%s' via reflection!", methodName);
        }

        return null;
    }

    /**
     * Execute a method of a given class.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static Object exec(Object obj, String methodName, Object... args) {
        if (obj instanceof Class<?>) { // Static.
            return exec(null, (Class<?>) obj, methodName, args);
        } else {
            return exec(obj, obj.getClass(), methodName, args);
        }
    }

    /**
     * Construct a class with no arguments.
     *
     * @param clazz The class to create an instance of.
     * @return object
     */
    @SuppressWarnings("unchecked")
    public static <T> T construct(Class<T> clazz) {
        Utils.verifyNotNull(clazz, "Tried to construct null class.");
        try {
            Constructor<T> emptyConstructor = (Constructor<T>) emptyConstructorCache.get(clazz);
            if (emptyConstructor == null) {
                emptyConstructorCache.put(clazz, emptyConstructor = clazz.getDeclaredConstructor(EMPTY_CLASS_ARRAY));
                emptyConstructor.setAccessible(true);
            }

            return emptyConstructor.newInstance(EMPTY_OBJECT_ARRAY);
        } catch (Throwable th) {
            Testing.warn("Could not construct %s, does it have a no-args constructor?", clazz.getSimpleName());
            throw new RuntimeException(th);
        }
    }

    /**
     * Construct a class with the given arguments.
     * Creates a fair bit of objects.
     *
     * @param clazz The class to create an instance of.
     * @param args  The arguments to construct the class with.
     * @return object
     */
    @SuppressWarnings("rawtypes")
    public static <T> T constructWithArgs(Class<T> clazz, Object... args) {
        Utils.verifyNotNull(clazz, "Tried to construct null class.");
        Class[] argTypes = getClasses(false, args);
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor(argTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Throwable th) {
            Testing.warn("Could not construct %s. (%d Arguments)", clazz.getSimpleName(), args.length);
            throw new RuntimeException(th);
        }
    }

    /**
     * Force construct a class with default or null values, if possible.
     *
     * @param clazz The class to construct.
     * @return constructed
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T forceConstruct(Class<T> clazz) {
        try {
            Constructor c = null;
            int minParams = Integer.MAX_VALUE;
            for (Constructor cls : clazz.getDeclaredConstructors()) {
                if (cls.getParameterCount() < minParams) {
                    minParams = cls.getParameterCount();
                    c = cls;
                }
            }

            if (minParams == Integer.MAX_VALUE) {
                Testing.warn("Failed to force-construct %s. No constructor found.", clazz.getSimpleName());
                return null;
            }

            Object[] args = new Object[minParams];
            for (int i = 0; i < minParams; i++) {
                Class<?> cls = c.getParameterTypes()[i];
                if (ReflectionUtil.getNumbers().contains(cls)) {
                    args[i] = 0;
                } else if (Boolean.class.isAssignableFrom(cls)) {
                    args[i] = false;
                }
            }

            c.setAccessible(true);
            return (T) c.newInstance(args);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException ex) {
            //noinspection CallToPrintStackTrace
            ex.printStackTrace();
            Testing.warn("Failed to force-construct %s!", clazz.getSimpleName());
            return null;
        }
    }

    /**
     * Get an array of classes from the arrays of the objects supplied.
     *
     * @param objects The objects to get classes from.
     * @return classes
     */
    private static Class<?>[] getClasses(boolean allowNull, Object[] objects) {
        if (objects == null || objects.length == 0)
            return EMPTY_CLASS_ARRAY;

        Class<?>[] classes = new Class<?>[objects.length];
        for (int i = 0; i < objects.length; i++) {
            Object current = objects[i];
            if (current != null) {
                classes[i] = getReplacedClass(current.getClass());
            } else if (!allowNull) {
                throw new GeneralException("Tried to get the class of a null object!");
            }
        }

        return classes;
    }

    /**
     * Set a field in an object.
     *
     * @param o       The object to set the field in.
     * @param from    The class the field is defined in.
     * @param varName The name of the field.
     * @param val     The value to set.
     */
    public static void setField(Object o, Class<?> from, String varName, Object val) {
        try {
            findField(from, varName).set(o, val);
        } catch (Throwable th) {
            Testing.handleError(th, "Failed to set field '%s' via reflection.", varName);
        }
    }

    /**
     * Set a field value in an object.
     *
     * @param o       The object to modify.
     * @param varName The name of the field to change.
     * @param val     The new value to set.
     */
    public static void setField(Object o, String varName, Object val) {
        setField(o, o.getClass(), varName, val);
    }

    /**
     * Set a static field in an object.
     *
     * @param clazz   The class the static field is defined in.
     * @param varName The name of the static field.
     * @param newVal  The value to set.
     */
    public static void setStaticField(Class<?> clazz, String varName, Object newVal) {
        setField(null, clazz, varName, newVal);
    }

    /**
     * Get a static value from a class.
     *
     * @param clazz The class which owns the field
     * @param field The name of the field to get.
     * @return value
     */
    public static Object getField(Class<?> clazz, String field) {
        return getField(null, clazz, field);
    }

    /**
     * Gets a value from an object.
     *
     * @param o     The object to get the value from.
     * @param field The name of the field to get.
     * @return fieldValue
     */
    public static Object getField(Object o, String field) {
        return getField(o, o.getClass(), field);
    }

    /**
     * Gets a value of a specific name from an object that is a certain class.
     *
     * @param o         The object to get the value from.
     * @param clazz     The object's class (that is translated to bytecode).
     * @param fieldName The name of the value field to get.
     * @return fieldValue
     */
    @SuppressWarnings("rawtypes")
    public static Object getField(Object o, Class clazz, String fieldName) {
        try {
            return findField(clazz, fieldName).get(o);
        } catch (Throwable th) {
            throw new GeneralException(th, "Failed to get value from field '" + fieldName + "'.");
        }
    }

    /**
     * Find a field by its name.
     *
     * @param clazz     The class to get the field from.
     * @param fieldName The name of the field to get.
     * @return field
     */
    public static Field findField(Class<?> clazz, String fieldName) {
        Field field = cacheFields(clazz).get(fieldName);
        if (field == null)
            throw new GeneralException("Unknown field '" + fieldName + "' in " + clazz.getSimpleName() + ".");

        return field;
    }


    /**
     * Search for a field in a class and its parents.
     *
     * @param clazz     The class to search.
     * @param fieldName The name of the field to get.
     * @return field - Throws an exception if not found.
     */
    public static Field findFieldIncludeParents(Class<?> clazz, String fieldName) {
        Class<?> startClass = clazz;
        while (!clazz.equals(Object.class)) {
            Field field = cacheFields(clazz).get(fieldName);
            if (field != null)
                return field;

            clazz = clazz.getSuperclass();
        }

        throw new RuntimeException("Unknown field '" + fieldName + "' in " + startClass.getSimpleName() + " (or its parents).");
    }

    /**
     * Get a list of all inherited and non-inherited fields.
     *
     * @param cls The class to get fields from.
     * @return fields
     */
    public static List<Field> getAllFields(Class<?> cls) {
        List<Field> fields = getJustClassFields(cls);
        Class<?> superClass = cls.getSuperclass();
        if (superClass != null)
            fields.addAll(getAllFields(superClass));

        return fields;
    }

    private static List<Field> getJustClassFields(Class<?> cls) {
        List<Field> fields = new ArrayList<>();
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            fields.add(field);
        }

        return fields;
    }

    private static Map<String, Field> cacheFields(Class<?> cls) {
        Map<String, Field> fieldMap = fieldCacheMap.get(cls);
        if (fieldMap != null)
            return fieldMap;

        fieldMap = new HashMap<>();
        for (Field field : cls.getDeclaredFields()) {
            field.setAccessible(true);
            fieldMap.put(field.getName(), field);
        }

        fieldCacheMap.put(cls, fieldMap);
        return fieldMap;
    }

    /**
     * Get a class by its path.
     *
     * @param path The path of the class to get.
     * @return class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String path) {
        try {
            Class<?> resultClass = classCacheMap.get(path);
            if (resultClass == null) {
                String realName = path;
                if (realName.contains("<"))
                    realName = realName.substring(0, realName.indexOf("<")); // Remove any generics.
                if (realName.startsWith("class ")) // Remove the "class " prefix that prepends the classpath when gotten from Type.
                    realName = realName.substring("class ".length());

                resultClass = Class.forName(realName);
                classCacheMap.put(path, resultClass);
                if (!path.equals(realName))
                    classCacheMap.put(realName, resultClass);
            }

            return (Class<T>) resultClass;
        } catch (Throwable th) {
            throw new GeneralException(th, "Failed to find Class by class path! (%s)", path);
        }
    }

    /**
     * Get a class by its type.
     *
     * @param type The type to get the class from.
     * @return class
     */
    public static Class<?> getClass(Type type) {
        if (type == null)
            return null;
        if (type instanceof Class<?>)
            return (Class<?>) type;

        // Fail-safe. May not always return a good value, such as when "int[]" is the result of getTypeName().
        return getClass(type.getTypeName());
    }

    /**
     * Get the generic type, of a field.
     * If there is no generic type, it will return null.
     *
     * @param f The field which has a generic type.
     * @return generic
     */
    public static Class<?> getGenericType(Field f) {
        if (f == null || !(f.getGenericType() instanceof ParameterizedType parameterizedType))
            return null;

        Type[] typeArguments = parameterizedType.getActualTypeArguments();
        if (typeArguments.length == 0)
            return null;

        return getClass(typeArguments[0]);
    }

    public static Collection<Class<?>> getNumbers() {
        return PRIMITIVES.values();
    }

    /**
     * Gets the default primitive of a type.
     *
     * @param type The type class to get the primitive of.
     * @return primitive
     */
    public static Class<?> getPrimitive(Class<?> type) {
        return PRIMITIVES.getOrDefault(type, type);
    }

    /**
     * Gets the replaced class of a primitive type.
     *
     * @param type The type.
     * @return replacedPrimitiveClass
     */
    public static Class<?> getReplacedClass(Class<?> type) {
        return REPLACE.getOrDefault(type, getPrimitive(type));
    }

    static {
        PRIMITIVES.put(Boolean.class, Boolean.TYPE);
        PRIMITIVES.put(Byte.class, Byte.TYPE);
        PRIMITIVES.put(Character.class, Character.TYPE);
        PRIMITIVES.put(Short.class, Short.TYPE);
        PRIMITIVES.put(Integer.class, Integer.TYPE);
        PRIMITIVES.put(Long.class, Long.TYPE);
        PRIMITIVES.put(Float.class, Float.TYPE);
        PRIMITIVES.put(Double.class, Double.TYPE);

        classCacheMap.put(Boolean.TYPE.getName(), Boolean.TYPE);
        classCacheMap.put(Byte.TYPE.getName(), Byte.TYPE);
        classCacheMap.put(Character.TYPE.getName(), Character.TYPE);
        classCacheMap.put(Short.TYPE.getName(), Short.TYPE);
        classCacheMap.put(Integer.TYPE.getName(), Integer.TYPE);
        classCacheMap.put(Long.TYPE.getName(), Long.TYPE);
        classCacheMap.put(Float.TYPE.getName(), Float.TYPE);
        classCacheMap.put(Double.TYPE.getName(), Double.TYPE);
    }
}