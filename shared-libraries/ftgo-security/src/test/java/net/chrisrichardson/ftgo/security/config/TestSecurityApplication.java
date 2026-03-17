package net.chrisrichardson.ftgo.security.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Minimal Spring Boot application for security integration tests.
 */
@SpringBootApplication(scanBasePackages = "net.chrisrichardson.ftgo.security")
public class TestSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestSecurityApplication.class, args);
    }

    @RestController
    static class TestController {

        @GetMapping("/api/test")
        public ResponseEntity<String> test() {
            return ResponseEntity.ok("OK");
        }

        @GetMapping("/api/protected")
        public ResponseEntity<String> protectedEndpoint() {
            return ResponseEntity.ok("Protected");
        }
    }
}
