package com.quadrah.sims.config;

import com.quadrah.sims.model.UserAccount;
import com.quadrah.sims.service.UserAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class KeycloakUserSyncFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(KeycloakUserSyncFilter.class);

    private final UserAccountService userAccountService;

    public KeycloakUserSyncFilter(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // Skip filter for certain paths
        if (requestURI.startsWith("/debug") ||
                requestURI.startsWith("/health") ||
                requestURI.startsWith("/swagger") ||
                requestURI.startsWith("/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication instanceof JwtAuthenticationToken) {

            try {
                JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
                Jwt jwt = jwtAuth.getToken();

                String keycloakId = jwt.getSubject();
                logger.info("Syncing user with Keycloak ID: {}", keycloakId);

                // Extract user info from JWT
                String username = jwt.getClaimAsString("preferred_username");
                String email = jwt.getClaimAsString("email");
                String firstName = jwt.getClaimAsString("given_name");
                String lastName = jwt.getClaimAsString("family_name");

                // If names are not in standard claims, try from name claim
                if (firstName == null || lastName == null) {
                    String fullName = jwt.getClaimAsString("name");
                    if (fullName != null && !fullName.trim().isEmpty()) {
                        String[] nameParts = fullName.split(" ", 2);
                        firstName = nameParts[0];
                        lastName = nameParts.length > 1 ? nameParts[1] : "";
                    } else {
                        // Use username as fallback
                        firstName = username != null ? username : "User";
                        lastName = "";
                    }
                }

                // Get role from JWT claims
                UserAccount.UserRole role = extractRoleFromJwt(jwt);
                logger.info("Extracted role for user {}: {}", keycloakId, role);

                // Sync user to local database
                UserAccount user = userAccountService.createOrUpdateUserFromKeycloak(
                        keycloakId,
                        username != null ? username : keycloakId,
                        email != null ? email : "",
                        firstName != null ? firstName : "Unknown",
                        lastName != null ? lastName : "User",
                        role != null ? role : UserAccount.UserRole.TEACHER
                );

                logger.info("Successfully synced user: {} (ID: {})", user.getUsername(), user.getId());

            } catch (Exception e) {
                logger.error("Failed to sync user from Keycloak", e);
                // Continue processing even if sync fails
            }
        }

        filterChain.doFilter(request, response);
    }

    private UserAccount.UserRole extractRoleFromJwt(Jwt jwt) {
        try {
            // Try to get roles from realm_access
            Object realmAccess = jwt.getClaim("realm_access");
            if (realmAccess instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> realmMap = (Map<String, Object>) realmAccess;
                Object rolesObj = realmMap.get("roles");
                if (rolesObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) rolesObj;
                    logger.debug("Realm roles found: {}", roles);

                    if (roles.contains("admin")) return UserAccount.UserRole.ADMIN;
                    if (roles.contains("nurse")) return UserAccount.UserRole.NURSE;
                    if (roles.contains("teacher")) return UserAccount.UserRole.TEACHER;
                }
            }

            // Try resource_access
            Object resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> resourceMap = (Map<String, Object>) resourceAccess;

                // Check for different possible client names
                String[] possibleClients = {"sims-backend", "backend", "springboot-app"};
                for (String client : possibleClients) {
                    Object clientAccess = resourceMap.get(client);
                    if (clientAccess instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> clientMap = (Map<String, Object>) clientAccess;
                        Object rolesObj = clientMap.get("roles");
                        if (rolesObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<String> roles = (List<String>) rolesObj;
                            logger.debug("Client roles found for {}: {}", client, roles);

                            if (roles.contains("admin")) return UserAccount.UserRole.ADMIN;
                            if (roles.contains("nurse")) return UserAccount.UserRole.NURSE;
                            if (roles.contains("teacher")) return UserAccount.UserRole.TEACHER;
                        }
                    }
                }
            }

            logger.warn("No roles found in JWT for user: {}", jwt.getSubject());

        } catch (Exception e) {
            logger.error("Error extracting roles from JWT", e);
        }

        return UserAccount.UserRole.NURSE; // Default role
    }
}