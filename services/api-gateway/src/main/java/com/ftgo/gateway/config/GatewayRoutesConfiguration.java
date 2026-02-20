package com.ftgo.gateway.config;

import com.ftgo.gateway.filter.JwtAuthenticationGatewayFilterFactory;
import com.ftgo.gateway.ratelimit.RateLimitGatewayFilterFactory;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRoutesConfiguration {

    private final GatewayProperties gatewayProperties;
    private final JwtAuthenticationGatewayFilterFactory jwtFilterFactory;
    private final RateLimitGatewayFilterFactory rateLimitFilterFactory;

    public GatewayRoutesConfiguration(GatewayProperties gatewayProperties,
                                      JwtAuthenticationGatewayFilterFactory jwtFilterFactory,
                                      RateLimitGatewayFilterFactory rateLimitFilterFactory) {
        this.gatewayProperties = gatewayProperties;
        this.jwtFilterFactory = jwtFilterFactory;
        this.rateLimitFilterFactory = rateLimitFilterFactory;
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        RouteLocatorBuilder.Builder routes = builder.routes();

        for (GatewayProperties.ServiceRoute serviceRoute : gatewayProperties.getRoutes()) {
            routes.route(serviceRoute.getId(), r -> r
                    .path(serviceRoute.getPath())
                    .filters(f -> {
                        f.stripPrefix(serviceRoute.getStripPrefix());

                        if (serviceRoute.isAuthRequired()) {
                            JwtAuthenticationGatewayFilterFactory.Config jwtConfig =
                                    new JwtAuthenticationGatewayFilterFactory.Config();
                            jwtConfig.setRequiredRoles(serviceRoute.getRequiredRoles());
                            f.filter(jwtFilterFactory.apply(jwtConfig));
                        }

                        RateLimitGatewayFilterFactory.Config rlConfig =
                                new RateLimitGatewayFilterFactory.Config();
                        rlConfig.setRequestsPerSecond(serviceRoute.getRateLimit());
                        rlConfig.setBurstCapacity(serviceRoute.getBurstCapacity());
                        f.filter(rateLimitFilterFactory.apply(rlConfig));

                        return f;
                    })
                    .uri(serviceRoute.getUri()));
        }

        return routes.build();
    }
}
