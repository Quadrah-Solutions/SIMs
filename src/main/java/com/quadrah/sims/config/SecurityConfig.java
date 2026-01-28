package com.quadrah.sims.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final KeycloakUserSyncFilter keycloakUserSyncFilter;
    private final KeycloakJwtAuthenticationConverter keycloakJwtConverter;

    public SecurityConfig(KeycloakUserSyncFilter keycloakUserSyncFilter,
                          KeycloakJwtAuthenticationConverter keycloakJwtConverter) {
        this.keycloakUserSyncFilter = keycloakUserSyncFilter;
        this.keycloakJwtConverter = keycloakJwtConverter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ ADD TEST ENDPOINTS HERE:
                        .requestMatchers("/api/test/**").permitAll()

                        // ✅ ADD HEALTH CHECK ENDPOINT:
                        .requestMatchers("/health", "/health/**", "/actuator/health").permitAll()

                        // Allow Swagger/OpenAPI endpoints without authentication
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api-docs/**",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/configuration/**"
                        ).permitAll()

                        // Allow public endpoints (if any)
                        .requestMatchers("/api/public/**").permitAll()

                        // Allow CORS preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Secure your API endpoints - UPDATED TO USE HASROLE WITH ROLE_ PREFIX
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/visits/**").hasAnyRole("NURSE", "ADMIN")
                        .requestMatchers("/api/medications/**").hasAnyRole("NURSE", "ADMIN")
                        .requestMatchers("/api/students/**").hasAnyRole("NURSE", "ADMIN", "TEACHER")
                        .requestMatchers("/api/notifications/**").hasAnyRole("NURSE", "ADMIN", "TEACHER")

                        // Grades and classes endpoints
                        .requestMatchers("/api/grades/**").hasAnyRole("NURSE", "ADMIN", "TEACHER")
                        .requestMatchers("/api/classes/**").hasAnyRole("NURSE", "ADMIN", "TEACHER")

                        // Users endpoints
                        .requestMatchers("/api/users/**").authenticated()

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                )
                .addFilterAfter(keycloakUserSyncFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}