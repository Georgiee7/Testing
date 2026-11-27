package me.george.testing.utils.range;

import me.george.testing.utils.Utils;

/**
 * A value written as text that can both test whether a value matches it, and roll a value that does.
 * The same text then works for searching ("level>=30") and for creating ("level=5-10").
 * Implementations: a closed {@link Range} ("5", "1-4"), an open {@link ComparisonMatcher} (">=3", "<10") and a {@link ValueSet} ("{a,b,c}").
 * Created by George K. on 11/27/2026.
 */
public interface ValueMatcher<T extends Comparable<T>> {

    /**
     * Whether a value matches this matcher.
     *
     * @param value The value to test.
     * @return matches
     */
    boolean contains(T value);

    /**
     * Rolls a value that matches this matcher. Open numeric comparisons like ">50" have no top to roll up to, so they throw,
     * they're only for matching. Closed ranges, sets and enum comparisons can all roll.
     *
     * @return rolledValue
     */
    T getRandomValue();

    /**
     * This matcher written the way it's parsed, so parsing it again gives the same matcher.
     *
     * @return configString
     */
    String toConfigString();

    /**
     * A friendly description of what this matcher accepts, such as "Fire or Water", "5-10" or "30+".
     *
     * @return description
     */
    String describe();

    /**
     * Formats a single value for describing it: enum values are capitalized, anything else is as it is.
     *
     * @param value The value to format.
     * @return displayValue
     */
    static String describeValue(Object value) {
        return value instanceof Enum<?> constant ? Utils.capitalize(constant.name()) : String.valueOf(value);
    }

    /**
     * Parses a value matcher: "5" (single value), "1-4" (range), ">=3", "<=3", ">3" or "<3" (comparison), or "{a,b,c}" (set).
     *
     * @param input The text to parse.
     * @param type  The type of the values, numeric or an enum.
     * @return valueMatcher, or null if the text is empty.
     */
    static <T extends Comparable<T>> ValueMatcher<T> parse(String input, Class<T> type) {
        if (input == null || input.isBlank())
            return null;

        String trimmed = input.trim();
        if (trimmed.startsWith("{") && trimmed.endsWith("}"))
            return ValueSet.parse(trimmed, type);

        ComparisonMatcher.Operator operator = ComparisonMatcher.Operator.detect(trimmed);
        if (operator != null)
            return new ComparisonMatcher<>(operator, Range.parseValue(trimmed.substring(operator.getSymbol().length()), type));

        return Range.parseFromString(trimmed, type);
    }
}
