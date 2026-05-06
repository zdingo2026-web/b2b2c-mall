package com.mall.common.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Serializes Long values that exceed JavaScript's Number.MAX_SAFE_INTEGER (2^53-1)
 * as Strings to prevent precision loss in the frontend.
 * Snowflake IDs (19 digits) exceed JS safe integer range, so they must be strings.
 */
public class SafeLongSerializer extends JsonSerializer<Long> {

    private static final long JS_MAX_SAFE_INTEGER = (1L << 53) - 1;

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value != null && (value > JS_MAX_SAFE_INTEGER || value < -JS_MAX_SAFE_INTEGER)) {
            gen.writeString(value.toString());
        } else {
            gen.writeNumber(value);
        }
    }
}
