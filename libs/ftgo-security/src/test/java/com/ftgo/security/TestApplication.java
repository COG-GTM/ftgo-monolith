package com.ftgo.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class TestApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestApplication.class, args);
    }

    @RestController
    static class TestController {

        @GetMapping("/api/test")
        public String test() {
            return "ok";
        }

        @GetMapping("/actuator/health")
        public String health() {
            return "UP";
        }
    }
}
