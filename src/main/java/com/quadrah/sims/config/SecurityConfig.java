package com.quadrah.sims.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.Customizer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.keycloak.public-url:https://192.168.8.102}")
    private String publicKeycloakUrl;

    // Inject the converter bean
    private final KeycloakJwtAuthenticationConverter keycloakJwtConverter;

    // Add constructor to inject dependencies
    public SecurityConfig(KeycloakJwtAuthenticationConverter keycloakJwtConverter) {
        this.keycloakJwtConverter = keycloakJwtConverter;

        // For development: Disable SSL verification (remove in production)
        disableSslVerification();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        System.out.println("DEBUG: Creating JwtDecoder");
        System.out.println("DEBUG: Using JWKS URI: " + jwkSetUri);
        System.out.println("DEBUG: Public Keycloak URL: " + publicKeycloakUrl);
        System.out.println("DEBUG: Configured issuer URI: " + issuerUri);

        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .build();

        // Create a custom validator that accepts multiple issuer URLs
        OAuth2TokenValidator<Jwt> validator = createCustomJwtValidator();

        jwtDecoder.setJwtValidator(validator);

        return jwtDecoder;
    }

    private OAuth2TokenValidator<Jwt> createCustomJwtValidator() {
        // Create a list of acceptable issuer URLs
        List<String> acceptableIssuers = Arrays.asList(
                publicKeycloakUrl + "/realms/SIMs",                          // https://192.168.8.102/realms/SIMs
                publicKeycloakUrl.replaceFirst("https://", "http://") + "/realms/SIMs", // http://192.168.8.102/realms/SIMs
                issuerUri,                                                   // http://keycloak:8080/auth/realms/SIMs
                "https://localhost/realms/SIMs",                            // localhost variants
                "http://localhost/realms/SIMs"
        );

        System.out.println("DEBUG: Acceptable issuers:");
        acceptableIssuers.forEach(issuer -> System.out.println("  - " + issuer));

        return new OAuth2TokenValidator<Jwt>() {
            @Override
            public OAuth2TokenValidatorResult validate(Jwt token) {
                String jwtIssuer = token.getIssuer().toString();
                System.out.println("DEBUG: Validating JWT with issuer: " + jwtIssuer);

                // Check if the issuer is in our acceptable list
                if (acceptableIssuers.contains(jwtIssuer)) {
                    System.out.println("DEBUG: Issuer accepted: " + jwtIssuer);
                    return OAuth2TokenValidatorResult.success();
                }

                // If not found, check if it's a variation we should accept
                for (String acceptableIssuer : acceptableIssuers) {
                    // Try to match with/without trailing slash
                    if (jwtIssuer.equals(acceptableIssuer) ||
                            jwtIssuer.equals(acceptableIssuer + "/") ||
                            (acceptableIssuer.endsWith("/") && jwtIssuer.equals(acceptableIssuer.substring(0, acceptableIssuer.length() - 1)))) {
                        System.out.println("DEBUG: Issuer accepted (with normalization): " + jwtIssuer);
                        return OAuth2TokenValidatorResult.success();
                    }
                }

                System.err.println("DEBUG: Invalid issuer. Expected one of:");
                acceptableIssuers.forEach(issuer -> System.err.println("  - " + issuer));
                System.err.println("Got: " + jwtIssuer);

                // Correct way to return failure - with OAuth2Error
                OAuth2Error error = new OAuth2Error(
                        "invalid_token",
                        "The issuer '" + jwtIssuer + "' is not in the list of acceptable issuers",
                        null
                );
                return OAuth2TokenValidatorResult.failure(error);
            }
        };
    }

    private void disableSslVerification() {
        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                            // Trust all
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                            // Trust all
                        }
                    }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = (hostname, session) -> true;

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            System.out.println("WARNING: SSL verification disabled! For development only!");

        } catch (Exception e) {
            System.err.println("Failed to disable SSL verification: " + e.getMessage());
        }
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Health checks
                        .requestMatchers("/health", "/actuator/health", "/api/health").permitAll()

                        // Swagger
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // OPTIONS for CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Your application endpoints
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/students/**").authenticated()
                        .requestMatchers("/api/grades/**").authenticated()
                        .requestMatchers("/api/classes/**").authenticated()

                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakJwtConverter))
                );

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .allowedOrigins(
                                extractOriginFromUrl(publicKeycloakUrl), // Extract origin from publicKeycloakUrl
                                "http://localhost:5173",
                                "http://localhost:80",
                                "http://localhost"
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600);
            }

            // Helper method to extract origin from a full URL
            private String extractOriginFromUrl(String url) {
                if (url == null || url.trim().isEmpty()) {
                    return "https://192.168.8.102"; // Fallback default
                }

                // Remove trailing slash if present
                url = url.trim();
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }

                // Remove path if present (keep only protocol://host:port)
                int pathIndex = url.indexOf("/", 8); // Start searching after "https://" or "http://"
                if (pathIndex > 0) {
                    url = url.substring(0, pathIndex);
                }

                System.out.println("DEBUG: Extracted origin from publicKeycloakUrl: " + url);
                return url;
            }
        };
    }
}