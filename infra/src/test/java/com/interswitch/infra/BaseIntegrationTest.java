package com.interswitch.infra;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = {TestApplication.class, TestConfig.class})
@ActiveProfiles("test")
@Transactional
public abstract class BaseIntegrationTest {

    protected DBConnectionProvider connectionProvider;

    void setUp() {
        // Common setup logic for all integration tests
    }
}