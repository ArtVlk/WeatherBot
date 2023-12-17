package org.matmeh.weatherbot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
class JacksonConfiguration {
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter() {
        return new MappingJackson2HttpMessageConverter(
                new Jackson2ObjectMapperBuilder()
                        .serializationInclusion(JsonInclude.Include.NON_NULL)
                        .failOnUnknownProperties(false)
                        .simpleDateFormat("HH:mm:ss dd.MM.yyyy")
                        .build()
        );
    }

    @Bean
    public Jackson2ObjectMapperBuilder jackson2ObjectMapperBuilder() {
        return new Jackson2ObjectMapperBuilder()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .simpleDateFormat("HH:mm:ss dd.MM.yyyy")
                .failOnUnknownProperties(false);
    }
}
