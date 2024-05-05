package org.example.xchange.configuration.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.xchange.configuration.security.xss.XSSSanitizer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.codec.HttpMessageDecoder;
import org.springframework.http.codec.HttpMessageEncoder;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.time.Instant;

@Configuration
public class JacksonConfig {


    public static final ObjectMapper JACKSON_OBJECT_MAPPER;

    static {
        ObjectMapper objectMapper = Jackson2ObjectMapperBuilder.json().build();
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true);

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Instant.class, RRInstantJacksonAdapter.instantJsonDeserializer);
        simpleModule.addSerializer(Instant.class, RRInstantJacksonAdapter.instantJsonSerializer);
        simpleModule.addDeserializer(String.class, new XSSSanitizer());
        objectMapper.registerModule(simpleModule);

        JavaTimeModule timeModule = new JavaTimeModule();
        objectMapper.registerModule(timeModule);

        JACKSON_OBJECT_MAPPER = objectMapper;
    }

    @Primary
    @Bean("jacksonObjectMapper")
    public ObjectMapper createJacksonObjectMapper() {
        return JACKSON_OBJECT_MAPPER;
    }

    @Bean
    public HttpMessageConverter<Object> jsonConverter(@Qualifier("jacksonObjectMapper") ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }

    @Bean
    public HttpMessageEncoder<Object> jsonEncoder(@Qualifier("jacksonObjectMapper") ObjectMapper objectMapper) {
        return new Jackson2JsonEncoder(objectMapper);
    }

    @Bean
    public HttpMessageDecoder<Object> jsonDecoder(@Qualifier("jacksonObjectMapper") ObjectMapper objectMapper) {
        return new Jackson2JsonDecoder(objectMapper);
    }

}
