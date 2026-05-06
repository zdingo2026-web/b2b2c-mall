package com.mall.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Web MVC configuration.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Custom ObjectMapper that serializes Long values exceeding JavaScript's
     * Number.MAX_SAFE_INTEGER as Strings to prevent precision loss.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        
        // Register JavaTimeModule for LocalDateTime serialization
        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DATE_TIME_FORMATTER));
        mapper.registerModule(javaTimeModule);
        
        // Register custom Long serialization/deserialization
        SimpleModule module = new SimpleModule();
        module.addSerializer(Long.class, new SafeLongSerializer());
        module.addSerializer(Long.TYPE, new SafeLongSerializer());
        module.addDeserializer(Long.class, new SafeLongDeserializer());
        module.addDeserializer(Long.TYPE, new SafeLongDeserializer());
        mapper.registerModule(module);
        
        return mapper;
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Ensure UTF-8 String converter
        converters.add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        // Replace the default Jackson converter with ours (using the custom ObjectMapper)
        for (int i = 0; i < converters.size(); i++) {
            if (converters.get(i) instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(objectMapper());
                jacksonConverter.setDefaultCharset(StandardCharsets.UTF_8);
                converters.set(i, jacksonConverter);
                break;
            }
        }
    }
}
