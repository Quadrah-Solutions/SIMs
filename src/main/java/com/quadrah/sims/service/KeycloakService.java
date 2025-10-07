package com.quadrah.sims.service;

import com.quadrah.sims.model.UserAccount;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakService {

    private final UserAccountService userAccountService;

    public KeycloakService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    public String getCurrentUserId() {
        Jwt jwt = getJwt();
        return jwt.getSubject();
    }

    public String getCurrentUsername() {
        Jwt jwt = getJwt();
        return jwt.getClaim("preferred_username");
    }

    public String getCurrentUserEmail() {
        Jwt jwt = getJwt();
        return jwt.getClaim("email");
    }

    public List<String> getCurrentUserRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public boolean hasRole(String role) {
        List<String> roles = getCurrentUserRoles();
        String roleWithPrefix = "ROLE_" + role.toUpperCase();
        return roles.contains(roleWithPrefix) || roles.contains(role.toUpperCase());
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

    public boolean isCurrentUser(Long userId) {
        UserAccount currentUser = userAccountService.getCurrentUser();
        return currentUser.getId().equals(userId);
    }

    private Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            return jwtAuth.getToken();
        }
        throw new IllegalStateException("Cannot get JWT from authentication");
    }
}