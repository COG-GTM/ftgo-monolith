package com.ftgo.security;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Shared test application for ftgo-security-lib integration tests.
 *
 * <p>Scans the {@code com.ftgo.security} package to pick up all
 * security configuration classes, handlers, and utilities.
 */
@SpringBootApplication
public class TestApplication {

    @RestController
    static class TestController {
        @GetMapping("/api/test")
        public String test() {
            return "OK";
        }
    }
}
