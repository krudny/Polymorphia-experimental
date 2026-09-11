package com.agh.polymorphia_backend.controller;

import com.agh.polymorphia_backend.BaseTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = "/test-data.sql"
)
public abstract class ControllerTestConfig extends BaseTest {
    protected static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    static {
        postgres.start();
    }

    @LocalServerPort
    protected int port;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("initial.user.firstName", () -> "Demo");
        registry.add("initial.user.lastName", () -> "User");
        registry.add("initial.user.email", () -> "demo@test.com");
        registry.add("initial.user.password", () -> "testPassword");

        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        registry.add("spring.mail.host", () -> "localhost");
        registry.add("spring.mail.port", () -> "9999");
        registry.add("spring.mail.username", () -> "test@example.com");
        registry.add("spring.mail.password", () -> "test-password");
        registry.add("spring.mail.properties.mail.smtp.auth", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.enable", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.starttls.required", () -> "false");
        registry.add("spring.mail.properties.mail.smtp.connectiontimeout", () -> "1000");
        registry.add("spring.mail.properties.mail.smtp.timeout", () -> "1000");
        registry.add("invitation.allow-multiple-emails", () -> "false");
        registry.add("spring.mail.registerUrl", () -> "https://sampleurl.com/registerUrl");

        registry.add("code-executor.base-url", () -> "http://localhost:8100");
        registry.add("code-executor.read-timeout-seconds", () -> "25");
        registry.add("code-executor.max-wall-time-limit-ms", () -> "20000");

        registry.add("task.submission.batch-size", () -> "4");
        registry.add("task.submission.worker-threads", () -> "2");
        registry.add("task.submission.lease-duration-seconds", () -> "120");
        registry.add("task.submission.max-processing-attempts", () -> "3");
        registry.add("task.submission.grading-stale-after-seconds", () -> "60");
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }
}
