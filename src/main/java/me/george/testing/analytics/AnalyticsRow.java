package me.george.testing.analytics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * A row of analytics being built, such as AnalyticsManager.track("damage").with("move", move).with("damage", 30).send().
 * When analytics is off, tracking gives the shared {@link #NONE} row, which ignores everything, so it costs next to nothing.
 * Created by George K. on 11/27/2026.
 */
public class AnalyticsRow {
    static final AnalyticsRow NONE = new AnalyticsRow(null); // Analytics is off: building and sending it does nothing.

    private final Map<String, Object> values; // Null for NONE.

    AnalyticsRow(String event) {
        this.values = event != null ? new LinkedHashMap<>() : null;
        if (this.values != null) {
            this.values.put("event", event);
            this.values.put("time", System.currentTimeMillis());
        }
    }

    /**
     * Adds a value to the row.
     *
     * @param key   The value's name.
     * @param value The value: a number, a boolean, or anything else, which is written as text. May be null.
     * @return thisRow
     */
    public AnalyticsRow with(String key, Object value) {
        if (this.values != null)
            this.values.put(key, value);

        return this;
    }

    /**
     * Sends the row to be written. It's only queued, so it never waits on the sink.
     */
    public void send() {
        if (this.values != null)
            AnalyticsManager.offer(toJson());
    }

    /**
     * Writes the row as a JSON object.
     *
     * @return json
     */
    public String toJson() {
        if (this.values == null)
            return "{}";

        StringJoiner json = new StringJoiner(",", "{", "}");
        this.values.forEach((key, value) -> json.add(toJsonValue(key) + ":" + toJsonValue(value)));
        return json.toString();
    }

    private static String toJsonValue(Object value) {
        if (value == null)
            return "null";

        if (value instanceof Boolean || (value instanceof Number number && Double.isFinite(number.doubleValue())))
            return value.toString();

        String text = value instanceof Enum<?> constant ? constant.name() : value.toString();
        StringBuilder json = new StringBuilder("\"");
        for (char character : text.toCharArray()) {
            switch (character) {
                case '"' -> json.append("\\\"");
                case '\\' -> json.append("\\\\");
                case '\n' -> json.append("\\n");
                case '\r' -> json.append("\\r");
                case '\t' -> json.append("\\t");
                default -> {
                    if (character < 0x20)
                        json.append(String.format("\\u%04x", (int) character));
                    else
                        json.append(character);
                }
            }
        }

        return json.append('"').toString();
    }
}
