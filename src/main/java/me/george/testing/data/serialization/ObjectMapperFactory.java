package me.george.testing.data.serialization;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Centralized factory providing a pre-configured Jackson {@link ObjectMapper}.
 * All serialization / deserialization in the project should use this single instance
 * to ensure consistent behaviour (module registration, feature flags, visibility rules).
 * Created by George K. on 2/26/2026.
 */
public final class ObjectMapperFactory {
    private static final ObjectMapper MAPPER = createMapper();

    private ObjectMapperFactory() {}

    /**
     * Returns the shared, pre-configured {@link ObjectMapper}.
     *
     * @return objectMapper
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    private static ObjectMapper createMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Serialization settings.
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Deserialization settings.
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // Visibility – rely on annotations + Lombok getters; don't auto-detect bare fields.
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        mapper.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.ANY);

        return mapper;
    }
}
