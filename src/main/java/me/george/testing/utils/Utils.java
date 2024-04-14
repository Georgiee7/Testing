package me.george.testing.utils;

import me.george.testing.Testing;
import me.george.testing.manager.PokemonManager;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A helper class of static utilities.
 * Created by George K. on 4/13/2024.
 */
public class Utils {

    /**
     * Capitalize every letter after a space.
     *
     * @param sentence The sentence to capitalize.
     * @return capitalized
     */
    public static String capitalize(String sentence) {
        String[] split = sentence.replaceAll("_", " ").split(" ");
        List<String> out = new ArrayList<>();
        for (String s : split)
            out.add(!s.isEmpty() ? s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase() : "");

        return String.join(" ", out);
    }

    /**
     * Verify a condition is true. If false, it will throw a GeneralException. This is safe to use, it won't allocate objects.
     *
     * @param condition The condition to verify.
     * @param error     The error description.
     */
    @SuppressWarnings("ConstantConditions") // Explained below.
    public static void verify(boolean condition, String error) {
        assert condition; // Our IDE will not give warnings handled by this method if use assert. However, we tell the JVM to disable assert so our own custom exceptions are used at run-time.
        if (!condition)
            throw new GeneralException(error);
    }

    /**
     * Verify a condition is true. If false, it will throw a GeneralException.
     * Be careful about using this function, because it makes lots of objects.
     *
     * @param condition The condition to verify.
     * @param error     The error description.
     * @param args      String format variables.
     */
    @SuppressWarnings("ConstantConditions") // Explained below.
    public static void verify(boolean condition, String error, Object... args) {
        assert condition; // Our IDE will not give warnings handled by this method if use assert. However, we tell the JVM to disable assert so our own custom exceptions are used at run-time.
        if (!condition)
            throw new GeneralException(Utils.format(error, args));
    }

    /**
     * Verify an object is not null. Otherwise, it will throw a GeneralException.
     * This is preferable to using var-args because it won't create an array object each time.
     *
     * @param object The object to confirm is not null.
     * @param error  The error
     */
    @SuppressWarnings("ConstantConditions")
    public static void verifyNotNull(Object object, String error) {
        assert object != null; // Our IDE will not give warnings handled by this method if use assert. However, we tell the JVM to disable assert so our own custom exceptions are used at run-time.
        if (object == null)
            throw new GeneralException(error);
    }

    /**
     * Format a string safely.
     *
     * @param str  The string to format.
     * @param args The arguments to format it with.
     * @return formattedString
     */
    public static String format(String str, Object... args) {
        if (args.length == 0)
            return str;

        try {
            return String.format(str, args);
        } catch (IllegalFormatException ife) {
            throw new GeneralException(ife, "Invalid string formatting for '" + str + "'.");
        }
    }

    /**
     * Format a string safely, not throwing an exception if there is an issue.
     *
     * @param str  The string to format.
     * @param args The arguments to format it with.
     * @return formattedString
     */
    public static String formatStringSafely(String str, Object... args) {
        if (args.length == 0)
            return str;
        try {
            return String.format(str, args);
        } catch (IllegalFormatException ife) {
            Testing.warn("There was an issue formatting the string " + str);
            return str + " (FORMATTING ERROR)";
        }
    }

    /**
     * Check if a given level is in the allowed level range of Pokémon.
     *
     * @param level The level to check.
     * @return isValidLevel
     */
    public static boolean isValidLevel(int level) {
        return level > 0 && level <= PokemonManager.MAX_LEVEL;
    }

    /**
     * Get the Random object.
     *
     * @return random
     */
    public static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * Get a random number between the given range. The min and max parameters are inclusive.
     *
     * @param min The minimum wanted number.
     * @param max The maximum wanted number.
     * @return int
     */
    public static int randInt(int min, int max) {
        if (min >= max)
            return max;
        return max + min > 0 ? nextInt(max - min + 1) + min : randInt(0, max - min) + min;
    }

    /**
     * Generate a random float between a float range.
     *
     * @param min The minimum wanted float.
     * @param max The maximum wanted float.
     * @return point
     */
    public static float randFloat(float min, float max) {
        return getRandom().nextFloat() * (max - min) + min;
    }

    /**
     * A random double between 0 and 1.
     *
     * @return randomDouble
     */
    public static double randDouble() {
        return getRandom().nextDouble();
    }

    /**
     * Generate a random number up until a given maximum.
     *
     * @param max The maximum wanted double.
     * @return rand
     */
    public static double nextDouble(double max) {
        return ThreadLocalRandom.current().nextDouble(max);
    }

    /**
     * Generate a random number between two doubles.
     *
     * @param min The minimum wanted double.
     * @param max The maximum wanted double.
     * @return rand
     */
    public static double randDouble(double min, double max) {
        return min == max ? min : ThreadLocalRandom.current().nextDouble(min, max);
    }

    /**
     * Generate a random number between two longs.
     *
     * @param min The minimum wanted long.
     * @param max The maximum wanted long.
     * @return rand
     */
    public static long randLong(long min, long max) {
        return min == max ? min : ThreadLocalRandom.current().nextLong(min, max);
    }

    /**
     * Get a random element from an array.
     *
     * @param arr The array to get a random element from.
     * @return element
     */
    public static <T> T randElement(T[] arr) {
        return arr.length == 0 ? null : arr[nextInt(arr.length)];
    }

    /**
     * Remove a random element from a list.
     *
     * @param list The list to get a random element from.
     * @return element
     */
    public static <T> T randRemoveElement(List<T> list) {
        return list.isEmpty() ? null : list.remove(nextInt(list.size()));
    }

    /**
     * Get a random element from a list.
     *
     * @param list The list to get a random element from.
     * @return element
     */
    public static <T> T randElement(List<T> list) {
        return randElement(list, null);
    }

    /**
     * Get a random element from a list.
     *
     * @param list     The collection to get a random element from.
     * @param fallback The fallback value to return if there are no values.
     * @return element
     */
    public static <T> T randElement(List<T> list, T fallback) {
        return list.isEmpty() ? fallback : list.get(nextInt(list.size()));
    }

    /**
     * Get a random element from a list.
     *
     * @param collection The collection to get a random element from.
     * @return element
     */
    public static <T> T randElement(Collection<T> collection) {
        return randElement(collection, null);
    }

    /**
     * Get a random element from a list.
     *
     * @param collection The collection to get a random element from.
     * @return element
     */
    public static <T> T randRemoveElement(Collection<T> collection) {
        if (collection.isEmpty())
            return null;

        T value = randElement(collection, null);
        collection.remove(value);
        return value;
    }

    /**
     * Get a random element from a list.
     *
     * @param collection The collection to get a random element from.
     * @param fallback   The fallback value to return if there are no values.
     * @return element
     */
    public static <T> T randElement(Collection<T> collection, T fallback) {
        if (collection.isEmpty())
            return fallback;

        int index = nextInt(collection.size());
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext() && index-- > 0)
            iterator.next();

        return iterator.next();
    }

    /**
     * Performs Random#nextDouble.
     *
     * @return randomDouble
     */
    public static double nextDouble() {
        return getRandom().nextDouble();
    }

    /**
     * Performs Random#nextLong.
     *
     * @return randomLong
     */
    public static double nextLong() {
        return getRandom().nextLong();
    }

    /**
     * Gets a random number between 0 and max - 1
     *
     * @param max The maximum value + 1. For instance, nextInt(3) would give values in the range [0,3].
     * @return rand
     */
    public static int nextInt(int max) {
        return getRandom().nextInt(max);
    }

    /**
     * Get a stream object from an Iterable.
     *
     * @param values The values to get a stream of.
     * @return stream
     */
    public static <T> Stream<T> stream(Iterable<T> values) {
        return StreamSupport.stream(values.spliterator(), false);
    }

    /**
     * Whether a string is null or empty.
     *
     * @param value The string in question.
     * @return isStringNullOrEmpty
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Is this input an integer?
     *
     * @param input Input that might be an integer.
     * @return integer
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isInteger(String input) {
        return input.startsWith("-") ? input.length() > 1 && StringUtils.isNumeric(input.substring(1)) : StringUtils.isNumeric(input);
    }

    /**
     * Get the last element of a list.
     *
     * @param list The list to get the element from.
     * @return lastElement
     */
    public static <T> T getLast(List<T> list) {
        return list.get(list.size() - 1);
    }

    /**
     * Removes and returns the last element of a list.
     *
     * @param list The list to remove the element from.
     * @return lastElement
     */
    public static <T> T removeLast(List<T> list) {
        return list.remove(list.size() - 1);
    }

    /**
     * Does the first list contain any elements from the second array?
     *
     * @param a The first collection to test.
     * @param b The second collection to test.
     * @return containsAny
     */
    public static <T> boolean containsAny(Collection<T> a, Collection<T> b) {
        for (T value : a)
            if (b.contains(value))
                return true;

        return false;
    }

    /**
     * Tests if an array contains a value. Creates no objects.
     *
     * @param array The array to search.
     * @param find  The value to look for. Can be null.
     * @return contains
     */
    @SuppressWarnings({"ForLoopReplaceableByForEach"})
    public static <T> boolean contains(T[] array, T find) {
        if (array == null || array.length == 0)
            return false;

        for (int i = 0; i < array.length; i++)
            if (Objects.equals(find, array[i]))
                return true;

        return false;
    }

    /**
     * Check if a string contains a substring regardless of case. Null-safe.
     *
     * @param s1        The parent string.
     * @param substring The sub-string we want to see if the parent contains.
     * @return contains
     */
    public static boolean containsIgnoreCase(String s1, String substring) {
        return s1 != null && substring != null && s1.toLowerCase().contains(substring.toLowerCase());
    }

    /**
     * Check if a collection of strings has an element which matches a string regardless of case.
     *
     * @param strings A collection of strings to search.
     * @param test    The string to test for.
     * @return containsIgnoreCase
     */
    public static boolean containsIgnoreCase(Collection<String> strings, String test) {
        return strings.stream().anyMatch(str -> str.equalsIgnoreCase(test));
    }

    /**
     * Gets the index of an element in an array, or -1 if it is not found.
     *
     * @param array   The array to search.
     * @param element The element to find.
     * @return foundIndex, or -1.
     */
    public static <T> int indexOf(T[] array, T element) {
        for (int i = 0; i < array.length; i++)
            if (Objects.equals(array[i], element))
                return i;

        return -1;
    }

    /**
     * Gets the simpleName of a class.
     *
     * @param clazz The class to get the simple name of.
     * @return simpleName
     */
    public static String getSimpleName(Class<?> clazz) {
        return clazz != null ? clazz.getSimpleName() : null;
    }

    /**
     * Gets the simpleName of an object.
     *
     * @param object The object to get the simple name of.
     * @return simpleName
     */
    public static String getSimpleName(Object object) {
        verifyNotNull(object, "Cannot get simple name of null object.");
        return getSimpleName(object.getClass());
    }

    /**
     * Gets the simpleName of an object.
     *
     * @param object The object to get the simple name of.
     * @return simpleName
     */
    public static String getSimpleNameAllowNull(Object object) {
        return object != null ? getSimpleName(object.getClass()) : "null";
    }

    /**
     * Gets the interfaces as a string implemented by a particular object.
     *
     * @param object the object to get classes from.
     */
    public static String getInterfaces(Object object) {
        return object != null ? getInterfaces(object.getClass()) : null;
    }

    /**
     * Gets the interfaces as a string implemented by a particular class.
     *
     * @param clazz the class to get classes from.
     */
    public static String getInterfaces(Class<?> clazz) {
        if (clazz == null)
            return null;

        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length == 0) {
            return "No Interfaces";
        } else if (interfaces.length == 1) {
            return interfaces[0].getSimpleName();
        } else {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < interfaces.length; i++) {
                if (i > 0)
                    builder.append(", ");
                builder.append(interfaces[i].getSimpleName());
            }

            return builder.toString();
        }
    }

    /**
     * Gets the first key whose indexed value matches the passed value.
     *
     * @param map   The map to search keys from.
     * @param value The value to search with.
     * @return indexKey
     */
    public static <K, V> K getKey(Map<K, V> map, V value) {
        for (K key : map.keySet())
            if (value.equals(map.get(key)))
                return key;

        return null;
    }

    /**
     * Returns if the number provided is negative or not.
     *
     * @param num The number to test.
     * @return isNegative
     */
    public static boolean isNegative(double num) {
        return Double.compare(num, 0.0) < 0;
    }
}
