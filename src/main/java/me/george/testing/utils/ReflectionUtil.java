package me.george.testing.utils;

import me.george.testing.Testing;

import java.lang.reflect.Constructor;
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
}