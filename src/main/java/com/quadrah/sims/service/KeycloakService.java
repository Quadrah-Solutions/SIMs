package com.quadrah.sims.service;

import com.quadrah.sims.model.UserAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakService {

    private final UserAccountService userAccountService;

    public KeycloakService(@Lazy UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public String getCurrentUserId() {
        KeycloakAuthenticationToken authentication =
                (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAccount().getKeycloakSecurityContext().getToken().getSubject();
    }

    public String getCurrentUsername() {
        KeycloakAuthenticationToken authentication =
                (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAccount().getKeycloakSecurityContext().getToken().getPreferredUsername();
    }

    public String getCurrentUserEmail() {
        KeycloakAuthenticationToken authentication =
                (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAccount().getKeycloakSecurityContext().getToken().getEmail();
    }

    public List<String> getCurrentUserRoles() {
        KeycloakAuthenticationToken authentication =
                (KeycloakAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public boolean hasRole(String role) {
        List<String> roles = getCurrentUserRoles();
        return roles.contains("ROLE_" + role.toUpperCase()) || roles.contains(role.toUpperCase());
    }

    public boolean isNurse() {
        return hasRole("NURSE") || hasRole("ADMIN");
    }

    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public boolean isTeacher() {
        return hasRole("TEACHER");
    }

    // Helper method for security expressions - use getCurrentUserId directly
    public boolean isCurrentUser(Long userId) {
        try {
            String currentKeycloakId = getCurrentUserId();
            UserAccount currentUser = userAccountService.getUserByKeycloakId(currentKeycloakId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            return currentUser.getId().equals(userId);
        } catch (Exception e) {
            return false;
        }
    }
}