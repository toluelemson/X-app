package org.example.xchange.configuration.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class RRInstantJacksonAdapter {

    public static final JsonDeserializer<Instant> instantJsonDeserializer = new JsonDeserializer<>() {
        @Override
        public Instant deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            TemporalAccessor temporalAccessor = dateTimeFormatter.parse(jsonParser.getText());
            return Instant.from(temporalAccessor);
        }
    };

    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_INSTANT;
    public static final JsonSerializer<Instant> instantJsonSerializer = new JsonSerializer<>() {
        @Override
        public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            String str = dateTimeFormatter.format(instant);
            jsonGenerator.writeString(str);
        }
    };

    private RRInstantJacksonAdapter() {
    }

}
