package com.mall.common.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Deserializes Long values from both number and string representations.
 * This allows the frontend to send snowflake IDs as strings (to avoid
 * JavaScript precision loss) while still supporting numeric values.
 */
public class SafeLongDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        switch (p.currentTokenId()) {
            case com.fasterxml.jackson.core.JsonTokenId.ID_NUMBER_INT:
                return p.getLongValue();
            case com.fasterxml.jackson.core.JsonTokenId.ID_STRING:
                String text = p.getText().trim();
                if (text.isEmpty()) {
                    return 0L;
                }
                return Long.parseLong(text);
            default:
                return ctxt.readValue(p, Long.class);
        }
    }
}
