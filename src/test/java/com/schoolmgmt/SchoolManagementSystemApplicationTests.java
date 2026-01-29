package com.schoolmgmt;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application context test to verify Spring Boot starts correctly.
 * Disabled: requires a running database (use integration test profile).
 */
@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires database connection - run with integration test profile")
class SchoolManagementSystemApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
    }
}
