package me.george.testing.utils;

import me.george.testing.Testing;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Static utils for using reflection.
 * Created by George K. on 10/22/2023.
 */
public class ReflectionUtil {

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final Map<Class<?>, Constructor<?>> emptyConstructorCache = new ConcurrentHashMap<>();

    private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

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
            throw new GeneralException(th, "Could not construct %s, does it have a no-args constructor?", clazz.getSimpleName());
        }
    }

    /**
     * Gets a method handle of a method, even a private one. Unlike Method#invoke, calling it throws the method's own
     * exceptions instead of wrapping them, and it's faster once made, so callers should keep it.
     *
     * @param method The method to get a handle of.
     * @return methodHandle
     */
    public static MethodHandle unreflect(Method method) {
        Utils.verifyNotNull(method, "Tried to get a handle of a null method.");
        try {
            method.setAccessible(true);
            return MethodHandles.lookup().unreflect(method);
        } catch (Throwable th) {
            throw new GeneralException(th, "Could not access %s#%s.", Utils.getSimpleName(method.getDeclaringClass()), method.getName());
        }
    }
}