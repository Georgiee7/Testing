package me.george.testing.utils.range;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.utils.GeneralException;
import me.george.testing.utils.Utils;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A closed range of comparable values, numbers or values of the same enum, such as "5", "1-4" or "FIRE-WATER".
 * Enum ranges go by the order the values are declared in.
 * Created by George K. on 11/27/2026.
 */
@Getter
public class Range<T extends Comparable<T>> implements ValueMatcher<T> {
    private static final Pattern RANGE_PATTERN = Pattern.compile("([^-]+)-([^-]+)");

    private final T lowerBound;
    private final T upperBound;

    public Range(T lowerBound, T upperBound) {
        Utils.verify(lowerBound != null && upperBound != null, "Cannot make a range without both bounds.");
        Utils.verify(lowerBound.compareTo(upperBound) <= 0, "The lower bound %s can't be above the upper bound %s.", lowerBound, upperBound);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public Range(T value) {
        this(value, value);
    }

    @Override
    public boolean contains(T value) {
        return value != null && value.compareTo(this.lowerBound) >= 0 && value.compareTo(this.upperBound) <= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getRandomValue() {
        if (this.lowerBound instanceof Enum<?> lower && this.upperBound instanceof Enum<?> upper) { // Enums roll by their order.
            Enum<?>[] values = lower.getDeclaringClass().getEnumConstants();
            return (T) values[Utils.randInt(lower.ordinal(), upper.ordinal())];
        }

        NumericType numericType = NumericType.of(this.lowerBound.getClass());
        if (numericType == null)
            throw new GeneralException("Cannot roll a value of the range %s.", toConfigString());

        double lower = ((Number) this.lowerBound).doubleValue();
        double upper = ((Number) this.upperBound).doubleValue();
        double rolled = numericType.isWhole() ? Utils.randLong((long) lower, (long) upper + 1) : Utils.randDouble(lower, upper);
        return (T) numericType.getCaster().apply(rolled);
    }

    @Override
    public String toConfigString() {
        return this.lowerBound.equals(this.upperBound) ? valueToString(this.lowerBound) : valueToString(this.lowerBound) + "-" + valueToString(this.upperBound);
    }

    @Override
    public String describe() {
        return this.lowerBound.equals(this.upperBound) ? ValueMatcher.describeValue(this.lowerBound)
                : ValueMatcher.describeValue(this.lowerBound) + "-" + ValueMatcher.describeValue(this.upperBound);
    }

    @Override
    public String toString() {
        return "[" + toConfigString() + "]";
    }

    /**
     * Parses a range, such as "1-4", or a single value such as "5", which is a range of just that value.
     *
     * @param input The text to parse.
     * @param type  The type of the values.
     * @return range
     */
    public static <T extends Comparable<T>> Range<T> parseFromString(String input, Class<T> type) {
        Matcher matcher = RANGE_PATTERN.matcher(input.trim());
        if (matcher.matches())
            return new Range<>(parseValue(matcher.group(1), type), parseValue(matcher.group(2), type));

        return new Range<>(parseValue(input, type));
    }

    /**
     * Parses a single value of a type, a number or an enum's value.
     *
     * @param input The text to parse.
     * @param type  The type of the value.
     * @return value
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends Comparable<T>> T parseValue(String input, Class<T> type) {
        String trimmed = input.trim();
        if (type.isEnum()) {
            T value = (T) Utils.getEnum(trimmed, (Class) type);
            if (value == null)
                throw new GeneralException("'%s' is not a %s.", trimmed, Utils.getSimpleName(type));

            return value;
        }

        NumericType numericType = NumericType.of(type);
        if (numericType == null)
            throw new GeneralException("Values of type %s aren't supported, only numbers and enums are.", Utils.getSimpleName(type));

        try {
            return (T) numericType.getParser().apply(trimmed);
        } catch (NumberFormatException exception) {
            throw new GeneralException("'%s' is not a valid number.", trimmed);
        }
    }

    /**
     * Writes a single value the way it's parsed, using the name of enum values.
     *
     * @param value The value to write.
     * @return valueString
     */
    static String valueToString(Object value) {
        return value instanceof Enum<?> constant ? constant.name() : String.valueOf(value);
    }

    /**
     * The number types ranges support, with how to parse them and how to turn a rolled number into them.
     */
    @Getter
    @AllArgsConstructor
    private enum NumericType {
        INTEGER(Integer.class, Integer::valueOf, Number::intValue, true),
        LONG(Long.class, Long::valueOf, Number::longValue, true),
        DOUBLE(Double.class, Double::valueOf, Number::doubleValue, false),
        FLOAT(Float.class, Float::valueOf, Number::floatValue, false);

        private final Class<?> type;
        private final Function<String, Number> parser;
        private final Function<Number, Number> caster;
        private final boolean whole; // Whether it rolls whole numbers, including the upper bound.

        /**
         * Gets the numeric type of a class.
         *
         * @param type The class.
         * @return numericType, or null if it isn't a supported number.
         */
        private static NumericType of(Class<?> type) {
            for (NumericType numericType : values())
                if (numericType.getType() == type)
                    return numericType;

            return null;
        }
    }
}
