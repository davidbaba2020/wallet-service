package com.interswitch.tests.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.interswitch.infra.repositories", "com.interswitch.model.entities", "com.interswitch.core.services"})
@EnableJpaRepositories(basePackages = {"com.interswitch.infra.repositories"})
@EntityScan(basePackages = {"com.interswitch.model.entities"})
public class TestApplication {
    // Empty - just needed for component scanning
}
