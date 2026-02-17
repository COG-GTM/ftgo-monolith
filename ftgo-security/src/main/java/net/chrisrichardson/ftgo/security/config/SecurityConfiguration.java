package net.chrisrichardson.ftgo.security.config;

import net.chrisrichardson.ftgo.security.jwt.JwtAuthenticationFilter;
import net.chrisrichardson.ftgo.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Value("${ftgo.security.jwt.secret:ftgo-microservices-secret-key-change-in-production}")
    private String jwtSecret;

    @Value("${ftgo.security.jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    @Bean
    public JwtTokenProvider jwtTokenProvider() {
        return new JwtTokenProvider(jwtSecret, jwtExpirationMs);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/actuator/**").permitAll()
                .antMatchers("/swagger-ui/**", "/v2/api-docs/**").permitAll()
                .antMatchers(HttpMethod.POST, "/auth/**").permitAll()
                .antMatchers(HttpMethod.GET, "/restaurants/**").permitAll()
                .antMatchers("/orders/**").hasAnyRole("CONSUMER", "ADMIN")
                .antMatchers("/consumers/**").hasAnyRole("CONSUMER", "ADMIN")
                .antMatchers(HttpMethod.PUT, "/restaurants/**").hasAnyRole("RESTAURANT_OWNER", "ADMIN")
                .antMatchers("/couriers/**").hasAnyRole("COURIER", "ADMIN")
                .anyRequest().authenticated()
            .and()
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }
}
