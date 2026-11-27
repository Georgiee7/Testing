package me.george.testing.utils.range;

import me.george.testing.utils.GeneralException;
import me.george.testing.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * A set of allowed values, such as "{FIRE,WATER}". A value matches if it's any of them, and rolling picks one of them.
 * Created by George K. on 11/27/2026.
 */
public class ValueSet<T extends Comparable<T>> implements ValueMatcher<T> {
    private final List<T> values;

    public ValueSet(List<T> values) {
        Utils.verify(values != null && !values.isEmpty(), "Cannot make an empty value set.");
        this.values = List.copyOf(values);
    }

    @Override
    public boolean contains(T value) {
        return this.values.contains(value);
    }

    @Override
    public T getRandomValue() {
        return Utils.randElement(this.values);
    }

    @Override
    public String toConfigString() {
        List<String> parts = new ArrayList<>();
        for (T value : this.values)
            parts.add(Range.valueToString(value));

        return "{" + String.join(",", parts) + "}";
    }

    @Override
    public String describe() {
        List<String> parts = new ArrayList<>();
        for (T value : this.values)
            parts.add(ValueMatcher.describeValue(value));

        if (parts.size() <= 2)
            return String.join(" or ", parts);

        return String.join(", ", parts.subList(0, parts.size() - 1)) + " or " + Utils.getLast(parts);
    }

    /**
     * Parses a value set, written as "{a,b,c}".
     *
     * @param input The text to parse, with the braces.
     * @param type  The type of the values.
     * @return valueSet
     */
    static <T extends Comparable<T>> ValueSet<T> parse(String input, Class<T> type) {
        String inner = input.substring(1, input.length() - 1).trim();
        if (inner.isEmpty())
            throw new GeneralException("Empty value set: '%s'.", input);

        List<T> values = new ArrayList<>();
        for (String part : inner.split(","))
            values.add(Range.parseValue(part, type));

        return new ValueSet<>(values);
    }
}
