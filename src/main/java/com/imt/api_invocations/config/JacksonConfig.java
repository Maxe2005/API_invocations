package com.imt.api_invocations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Configuration Jackson pour assurer la correcte sérialisation/désérialisation
 * des objets Java vers JSONB PostgreSQL.
 * 
 * Configure notamment le support des types Java 8+ (LocalDateTime, etc.)
 * qui sont utilisés dans InvocationBufferDto et autres DTOs.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Support des types Java 8+ (LocalDateTime, LocalDate, etc.)
        mapper.registerModule(new JavaTimeModule());

        // Désactive la sérialisation des dates en timestamps
        // Préfère le format ISO-8601 (ex: "2026-02-08T10:30:00")
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
