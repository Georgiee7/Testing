package me.george.testing.utils;

import me.george.testing.Testing;
import me.george.testing.pokemon.Pokemon;

import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;

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
        return level > 0 && level <= Pokemon.MAX_LEVEL;
    }
}
