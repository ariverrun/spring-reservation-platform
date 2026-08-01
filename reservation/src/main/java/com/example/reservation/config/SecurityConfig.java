package com.example.reservation.config;

import com.example.reservation.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(this::configureAuthorization)
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

    return http.build();
}

    private void configureAuthorization(
        AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry auth
    ) {
        auth
            .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/v3/api-docs/*").permitAll()
            .requestMatchers(HttpMethod.GET, "/v3/api-docs**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/event").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/event/my").hasRole("ADMIN")
            .requestMatchers(HttpMethod.GET, "/api/v1/event/{id}").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/v1/event/{id}/reserve").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/event").hasRole("ADMIN")
            .requestMatchers(HttpMethod.PUT, "/api/v1/event/{id}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.DELETE, "/api/v1/event/{id}").hasRole("ADMIN")
            .requestMatchers(HttpMethod.POST, "/api/v1/reserve").hasRole("USER")
            .requestMatchers(HttpMethod.GET, "/api/v1/reserve").hasRole("USER")
            .requestMatchers(HttpMethod.GET, "/api/v1/reserve/{id}").hasRole("USER")
            .requestMatchers(HttpMethod.PUT, "/api/v1/reserve/{id}").hasRole("USER")
            .anyRequest().authenticated();
    }
}