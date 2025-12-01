package com.quadrah.sims.config;

import com.quadrah.sims.model.UserAccount;
import com.quadrah.sims.service.UserAccountService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserAccountService userAccountService;

    public KeycloakJwtAuthenticationConverter(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // Sync user to database first
        syncUserToDatabase(jwt);

        // Extract authorities from JWT
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);

        // Create and return the authentication token
        return new JwtAuthenticationToken(jwt, authorities);
    }

    private void syncUserToDatabase(Jwt jwt) {
        try {
            // Extract user info from JWT token
            String keycloakId = jwt.getSubject();
            String username = jwt.getClaim("preferred_username");
            String email = jwt.getClaim("email");
            String firstName = jwt.getClaim("given_name");
            String lastName = jwt.getClaim("family_name");

            // Determine role from Keycloak roles
            UserAccount.UserRole role = determineRoleFromJwt(jwt);

            // Sync user to database
            userAccountService.createOrUpdateUserFromKeycloak(
                    keycloakId, username, email, firstName, lastName, role
            );
        } catch (Exception e) {
            // Log the error but don't break authentication
            System.err.println("Failed to sync user to database: " + e.getMessage());
            // logger.error("Failed to sync user to database", e);
        }
    }

    private UserAccount.UserRole determineRoleFromJwt(Jwt jwt) {
        Set<String> allRoles = extractAllRoles(jwt);

        System.out.println("All extracted roles: " + allRoles);

        // Check for admin roles (case-insensitive)
        if (containsAnyRoleIgnoreCase(allRoles, "admin")) {
            System.out.println("Assigning ADMIN role");
            return UserAccount.UserRole.ADMIN;
        }

        // Check for nurse roles (case-insensitive)
        if (containsAnyRoleIgnoreCase(allRoles, "nurse")) {
            System.out.println("Assigning NURSE role");
            return UserAccount.UserRole.NURSE;
        }

        // Check for teacher roles (case-insensitive)
        if (containsAnyRoleIgnoreCase(allRoles, "teacher")) {
            System.out.println("Assigning TEACHER role");
            return UserAccount.UserRole.TEACHER;
        }

        System.out.println("No specific roles found, defaulting to TEACHER");
        return UserAccount.UserRole.TEACHER;
    }

    private Set<String> extractAllRoles(Jwt jwt) {
        Set<String> allRoles = new HashSet<>();

        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> realmRoles = (List<String>) realmAccess.get("roles");
            if (realmRoles != null) {
                allRoles.addAll(realmRoles);
            }
        }

        // Extract client roles from all clients (excluding account client)
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            for (String clientId : resourceAccess.keySet()) {
                // Skip the 'account' client as it only has management roles
                if ("account".equals(clientId)) {
                    continue;
                }
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get(clientId);
                if (clientAccess != null) {
                    List<String> clientRoles = (List<String>) clientAccess.get("roles");
                    if (clientRoles != null) {
                        allRoles.addAll(clientRoles);
                    }
                }
            }
        }

        return allRoles;
    }

    private boolean containsAnyRoleIgnoreCase(Set<String> roles, String... roleNames) {
        // Convert all roles to lowercase for case-insensitive comparison
        Set<String> lowerCaseRoles = roles.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        for (String roleName : roleNames) {
            if (lowerCaseRoles.contains(roleName.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract realm roles
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
            }
        }

        // Extract client-specific roles
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null) {
            Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("school-infirmary-client");
            if (clientAccess != null) {
                List<String> roles = (List<String>) clientAccess.get("roles");
                if (roles != null) {
                    roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));
                }
            }
        }

        return authorities;
    }
}