package me.george.testing.data.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.george.testing.utils.GeneralException;

/**
 * Convenience wrapper around the shared {@link ObjectMapper} for common
 * serialization and deserialization operations.
 * Created by George K. on 2/26/2026.
 */
public final class JsonUtil {
    private JsonUtil() {}

    /**
     * Serializes an object to its JSON string representation.
     *
     * @param value The object to serialize.
     * @return json
     */
    public static String toJson(Object value) {
        try {
            return ObjectMapperFactory.getMapper().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new GeneralException(e, "Failed to serialize object to JSON.");
        }
    }

    /**
     * Serializes an object to a pretty-printed JSON string representation.
     *
     * @param value The object to serialize.
     * @return prettyJson
     */
    public static String toPrettyJson(Object value) {
        try {
            return ObjectMapperFactory.getMapper().writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new GeneralException(e, "Failed to serialize object to pretty JSON.");
        }
    }

    /**
     * Deserializes a JSON string to an object of the specified type.
     *
     * @param json  The JSON string to deserialize.
     * @param clazz The target class.
     * @param <T>   The target type.
     * @return deserializedObject
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return ObjectMapperFactory.getMapper().readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new GeneralException(e, "Failed to deserialize JSON to %s.", clazz.getSimpleName());
        }
    }

    /**
     * Deserializes a JSON string to an object using a {@link TypeReference} for generic types.
     *
     * @param json          The JSON string to deserialize.
     * @param typeReference The type reference describing the target type.
     * @param <T>           The target type.
     * @return deserializedObject
     */
    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return ObjectMapperFactory.getMapper().readValue(json, typeReference);
        } catch (JsonProcessingException e) {
            throw new GeneralException(e, "Failed to deserialize JSON.");
        }
    }

    /**
     * Parses a JSON string into a {@link JsonNode} tree for manual traversal.
     *
     * @param json The JSON string to parse.
     * @return jsonNode
     */
    public static JsonNode readTree(String json) {
        try {
            return ObjectMapperFactory.getMapper().readTree(json);
        } catch (JsonProcessingException e) {
            throw new GeneralException(e, "Failed to parse JSON tree.");
        }
    }

    /**
     * Converts an object from one type to another via the ObjectMapper (object-to-object mapping).
     *
     * @param source    The source object.
     * @param targetClass The target class.
     * @param <T>       The target type.
     * @return convertedObject
     */
    public static <T> T convert(Object source, Class<T> targetClass) {
        return ObjectMapperFactory.getMapper().convertValue(source, targetClass);
    }
}
