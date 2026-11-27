package me.george.testing.utils.range;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.george.testing.utils.GeneralException;
import me.george.testing.utils.Utils;

/**
 * An open comparison, such as ">3", ">=3", "<3" or "<=3". Mostly for matching, like searching for "level>=30".
 * Only enums can roll a value from one, as they have a first and last value, so rolling from a numeric comparison throws.
 * Created by George K. on 11/27/2026.
 */
@Getter
@AllArgsConstructor
public class ComparisonMatcher<T extends Comparable<T>> implements ValueMatcher<T> {
    private final Operator operator;
    private final T value;

    @Override
    public boolean contains(T other) {
        if (other == null)
            return false;

        int comparison = other.compareTo(this.value);
        return switch (this.operator) {
            case GREATER_THAN -> comparison > 0;
            case GREATER_OR_EQUAL -> comparison >= 0;
            case LESS_THAN -> comparison < 0;
            case LESS_OR_EQUAL -> comparison <= 0;
        };
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getRandomValue() {
        if (!(this.value instanceof Enum<?> enumValue))
            throw new GeneralException("Cannot roll a value from the open comparison '%s', use a range like 'a-b' instead.", toConfigString());

        Enum<?>[] values = enumValue.getDeclaringClass().getEnumConstants();
        int ordinal = enumValue.ordinal();
        int lower = switch (this.operator) {
            case GREATER_THAN -> ordinal + 1;
            case GREATER_OR_EQUAL -> ordinal;
            case LESS_THAN, LESS_OR_EQUAL -> 0;
        };
        int upper = switch (this.operator) {
            case GREATER_THAN, GREATER_OR_EQUAL -> values.length - 1;
            case LESS_THAN -> ordinal - 1;
            case LESS_OR_EQUAL -> ordinal;
        };

        Utils.verify(lower <= upper, "The comparison '%s' matches no %s values.", toConfigString(), Utils.getSimpleName(enumValue.getDeclaringClass()));
        return (T) values[Utils.randInt(lower, upper)];
    }

    @Override
    public String toConfigString() {
        return this.operator.getSymbol() + Range.valueToString(this.value);
    }

    @Override
    public String describe() {
        String value = ValueMatcher.describeValue(this.value);
        return switch (this.operator) {
            case GREATER_OR_EQUAL -> value + "+";
            case GREATER_THAN -> "above " + value;
            case LESS_OR_EQUAL -> "up to " + value;
            case LESS_THAN -> "below " + value;
        };
    }

    /**
     * The comparison operators. The two character ones come first, so they're detected before the one character ones they start with.
     */
    @Getter
    @AllArgsConstructor
    public enum Operator {
        GREATER_OR_EQUAL(">="),
        LESS_OR_EQUAL("<="),
        GREATER_THAN(">"),
        LESS_THAN("<");

        private final String symbol;

        /**
         * Detects the operator a text starts with.
         *
         * @param input The text in question.
         * @return operator, or null if it doesn't start with one.
         */
        public static Operator detect(String input) {
            for (Operator operator : values())
                if (input.startsWith(operator.getSymbol()))
                    return operator;

            return null;
        }
    }
}
