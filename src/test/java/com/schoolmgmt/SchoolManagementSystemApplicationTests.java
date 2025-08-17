package com.schoolmgmt;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Application context test to verify Spring Boot starts correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class SchoolManagementSystemApplicationTests {

    @Test
    void contextLoads() {
        // This test verifies that the Spring application context loads successfully
    }
}
