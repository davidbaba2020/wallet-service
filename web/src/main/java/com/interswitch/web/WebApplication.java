package com.interswitch.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.interswitch","com.interswitch.core", "com.interswitch.security"})
@EntityScan("com.interswitch.model.entities")
@EnableJpaRepositories("com.interswitch.infra.repositories")
public class WebApplication {
    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}
