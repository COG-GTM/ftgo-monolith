package com.ftgo.security.authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication(scanBasePackages = "com.ftgo.security.authorization")
@Import({RoleHierarchyConfiguration.class, FtgoMethodSecurityConfiguration.class})
public class RbacTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RbacTestApplication.class, args);
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @RestController
    static class AdminController {

        @GetMapping("/api/admin/config")
        @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
        public String getConfig() {
            return "admin-config";
        }

        @DeleteMapping("/api/users/{id}")
        @PreAuthorize(RoleConstants.HAS_ROLE_ADMIN)
        public String deleteUser() {
            return "user-deleted";
        }
    }

    @RestController
    static class ManagerController {

        @GetMapping("/api/manager/reports")
        @PreAuthorize(RoleConstants.HAS_ANY_ROLE_ADMIN_MANAGER)
        public String getReports() {
            return "manager-reports";
        }

        @PutMapping("/api/restaurants/{id}")
        @PreAuthorize(RoleConstants.HAS_ANY_ROLE_ADMIN_MANAGER)
        public String updateRestaurant() {
            return "restaurant-updated";
        }
    }

    @RestController
    static class UserController {

        @GetMapping("/api/orders")
        @PreAuthorize(RoleConstants.HAS_ROLE_USER)
        public String getOrders() {
            return "orders-list";
        }

        @GetMapping("/actuator/health")
        public String health() {
            return "UP";
        }
    }

    @RestController
    static class ServiceController {

        @GetMapping("/api/internal/health")
        @PreAuthorize(RoleConstants.HAS_ROLE_SERVICE)
        public String serviceHealth() {
            return "service-health";
        }

        @PostMapping("/api/orders/sync")
        @PreAuthorize(RoleConstants.HAS_ANY_ROLE_ADMIN_SERVICE)
        public String syncOrders() {
            return "orders-synced";
        }
    }

    @RestController
    static class SecuredController {

        @GetMapping("/api/secured/admin")
        @Secured("ROLE_ADMIN")
        public String securedAdmin() {
            return "secured-admin";
        }
    }
}
