package com.interswitch.tests.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.activation.DataSource;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
public class IntegrationTestConfig {

    // Configure ObjectMapper for test serialization/deserialization
    @Bean
    @Primary
    public ObjectMapper testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    // In-memory test database configuration
    @Bean
    @Primary
    @Profile("test")
    public DataSource testDataSource() {
        return (DataSource) DataSourceBuilder
                .create()
                .driverClassName("org.h2.Driver")
                .url("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE")
                .username("sa")
                .password("")
                .build();
    }

    // Test data initialization (if needed)
    @Bean
    @Profile("test")
    public org.springframework.boot.CommandLineRunner testDataInitializer() {
        return args -> {
            // Initialize test data if needed
            // log.info("Initializing test data...");
        };
    }


    // Test event publisher (if you have async events)
    @Bean
    @Primary
    @Profile("test") 
    public SimpleApplicationEventMulticaster testEventPublisher() {
        return new org.springframework.context.event.SimpleApplicationEventMulticaster();
    }

    // Test cache configuration (disable caching in tests)
    @Bean
    @Primary
    @Profile("test")
    public org.springframework.cache.CacheManager testCacheManager() {
        return new org.springframework.cache.support.NoOpCacheManager();
    }
}