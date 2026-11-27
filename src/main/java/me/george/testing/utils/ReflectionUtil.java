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
     * Gets the fields of a class and of the classes above it that are marked with an annotation, made accessible.
     * Static fields are left out. This scans the class every time, so callers should keep the result.
     *
     * @param clazz      The class whose fields to get.
     * @param annotation The annotation the fields are marked with.
     * @return annotatedFields
     */
    public static List<Field> getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        Utils.verifyNotNull(clazz, "Tried to get the fields of a null class.");
        List<Field> fields = new ArrayList<>();
        for (Class<?> current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
            for (Field field : current.getDeclaredFields()) {
                if (!field.isAnnotationPresent(annotation) || Modifier.isStatic(field.getModifiers()))
                    continue;

                field.setAccessible(true);
                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Gets the class of a field's generic argument, which is otherwise erased. For a List&lt;String&gt; field, argument 0 is String.
     *
     * @param field The field.
     * @param index The index of the generic argument.
     * @return argumentClass
     */
    public static Class<?> getGenericArgument(Field field, int index) {
        Utils.verifyNotNull(field, "Tried to get a generic argument of a null field.");
        if (field.getGenericType() instanceof ParameterizedType parameterizedType) {
            Type[] arguments = parameterizedType.getActualTypeArguments();
            if (index >= 0 && index < arguments.length) {
                Type argument = arguments[index];
                if (argument instanceof Class<?> argumentClass)
                    return argumentClass;

                if (argument instanceof ParameterizedType parameterizedArgument)
                    return (Class<?>) parameterizedArgument.getRawType();
            }
        }

        throw new GeneralException("%s#%s has no generic argument %d that is a class.", Utils.getSimpleName(field.getDeclaringClass()), field.getName(), index);
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