package com.interswitch.tests.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TestApplication.class, TestcontainersConfiguration.class})
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

//    protected DBConnectionProvider connectionProvider;

    void setUp() {
        // Common setup logic for all integration tests
    }
}