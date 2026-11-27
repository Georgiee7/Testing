package me.george.testing.utils;

/**
 * A function that takes three arguments, like a BiFunction with one more.
 * Created by George K. on 11/27/2026.
 */
@FunctionalInterface
public interface TriFunction<S, T, U, R> {

    /**
     * Applies this function to the given arguments.
     *
     * @param first  The first argument.
     * @param second The second argument.
     * @param third  The third argument.
     * @return result
     */
    R apply(S first, T second, U third);
}
