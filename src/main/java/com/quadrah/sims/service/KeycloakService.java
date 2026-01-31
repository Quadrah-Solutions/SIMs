package com.quadrah.sims.service;

import com.quadrah.sims.model.UserAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(KeycloakService.class);

    private final UserAccountService userAccountService;
    private final RestTemplate restTemplate;

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.resource}")
    private String clientId;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret}")
    private String adminClientSecret;

    public KeycloakService(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Create a new user in Keycloak
     */
    public String createKeycloakUser(String username, String email, String password,
                                     String firstName, String lastName, UserAccount.UserRole role) {
        try {
            log.info("Creating Keycloak user: username={}, email={}, firstName={}, lastName={}, role={}",
                    username, email, firstName, lastName, role);

            // 1. Get admin access token
            String adminToken = getAdminAccessToken();
            log.debug("Admin token obtained");

            // 2. Create user in Keycloak
            String userId = createUserInKeycloak(adminToken, username, email, password, firstName, lastName);
            log.info("User created in Keycloak with ID: {}", userId);

            // 3. Assign role to the user
            assignRoleToUser(adminToken, userId, role);
            log.info("Role assigned to user");

            return userId;
        } catch (Exception e) {
            log.error("Error creating Keycloak user", e);
            throw new RuntimeException("Failed to create user in Keycloak: " + e.getMessage(), e);
        }
    }

    /**
     * Get admin access token for Keycloak Admin API
     */
    private String getAdminAccessToken() {
        String tokenUrl = keycloakServerUrl + "/realms/master/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = String.format("grant_type=client_credentials&client_id=%s&client_secret=%s",
                adminClientId, adminClientSecret);

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
            throw new RuntimeException("Failed to get admin access token from Keycloak");
        }

        return (String) response.getBody().get("access_token");
    }

    /**
     * Create user in Keycloak
     */
    private String createUserInKeycloak(String adminToken, String username, String email,
                                        String password, String firstName, String lastName) {

        String createUserUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put("username", username);
        userRequest.put("email", email);
        userRequest.put("firstName", firstName);
        userRequest.put("lastName", lastName);
        userRequest.put("enabled", true);
        userRequest.put("emailVerified", true);

        // Set credentials (password)
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", password);
        credentials.put("temporary", false);

        userRequest.put("credentials", Arrays.asList(credentials));

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userRequest, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(createUserUrl, request, Void.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to create user in Keycloak. Status: " + response.getStatusCode());
        }

        // Extract user ID from response headers (Keycloak returns location header with user ID)
        String locationHeader = response.getHeaders().getFirst(HttpHeaders.LOCATION);
        if (locationHeader == null) {
            throw new RuntimeException("No location header in response from Keycloak");
        }

        // Extract user ID from location header
        return locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
    }

    /**
     * Assign role to user in Keycloak
     */
    private void assignRoleToUser(String adminToken, String userId, UserAccount.UserRole role) {
        // 1. Get the role representation from Keycloak
        String roleId = getRoleId(adminToken, role);

        // 2. Assign role to user
        String assignRoleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + userId + "/role-mappings/realm";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> roleMapping = new HashMap<>();
        roleMapping.put("id", roleId);
        roleMapping.put("name", role.name());

        HttpEntity<List<Map<String, Object>>> request = new HttpEntity<>(Arrays.asList(roleMapping), headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(assignRoleUrl, request, Void.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT && response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to assign role to user. Status: " + response.getStatusCode());
        }
    }

    /**
     * Get role ID from Keycloak
     */
    private String getRoleId(String adminToken, UserAccount.UserRole role) {
        String rolesUrl = keycloakServerUrl + "/admin/realms/" + realm + "/roles/" + role.name();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(rolesUrl, HttpMethod.GET, request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("id");
            }
        } catch (Exception e) {
            // Role might not exist, create it
            return createRoleInKeycloak(adminToken, role);
        }

        throw new RuntimeException("Failed to get role ID for role: " + role.name());
    }

    /**
     * Create role in Keycloak if it doesn't exist
     */
    private String createRoleInKeycloak(String adminToken, UserAccount.UserRole role) {
        String createRoleUrl = keycloakServerUrl + "/admin/realms/" + realm + "/roles";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> roleRequest = new HashMap<>();
        roleRequest.put("name", role.name());
        roleRequest.put("description", role.name() + " role");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(roleRequest, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(createRoleUrl, request, Void.class);

        if (response.getStatusCode() != HttpStatus.CREATED) {
            throw new RuntimeException("Failed to create role in Keycloak. Status: " + response.getStatusCode());
        }

        // Now get the role ID
        return getRoleId(adminToken, role);
    }

    /**
     * Update user in Keycloak
     */
    public void updateKeycloakUser(String keycloakUserId, String email, String firstName, String lastName) {
        String adminToken = getAdminAccessToken();
        String updateUserUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("email", email);
        userUpdate.put("firstName", firstName);
        userUpdate.put("lastName", lastName);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userUpdate, headers);

        restTemplate.put(updateUserUrl, request);
    }

    /**
     * Reset user password in Keycloak
     */
    public void resetUserPassword(String keycloakUserId, String newPassword) {
        String adminToken = getAdminAccessToken();
        String resetPasswordUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId + "/reset-password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> passwordRequest = new HashMap<>();
        passwordRequest.put("type", "password");
        passwordRequest.put("value", newPassword);
        passwordRequest.put("temporary", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(passwordRequest, headers);

        ResponseEntity<Void> response = restTemplate.postForEntity(resetPasswordUrl, request, Void.class);

        if (response.getStatusCode() != HttpStatus.NO_CONTENT) {
            throw new RuntimeException("Failed to reset password. Status: " + response.getStatusCode());
        }
    }

    /**
     * Disable user in Keycloak
     */
    public void disableKeycloakUser(String keycloakUserId) {
        String adminToken = getAdminAccessToken();
        String updateUserUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("enabled", false);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userUpdate, headers);

        restTemplate.put(updateUserUrl, request);
    }

    /**
     * Enable user in Keycloak
     */
    public void enableKeycloakUser(String keycloakUserId) {
        String adminToken = getAdminAccessToken();
        String updateUserUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(adminToken);

        Map<String, Object> userUpdate = new HashMap<>();
        userUpdate.put("enabled", true);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userUpdate, headers);

        restTemplate.put(updateUserUrl, request);
    }

    /**
     * Get user info from Keycloak
     */
    public Map<String, Object> getUserInfo(String keycloakUserId) {
        String adminToken = getAdminAccessToken();
        String userUrl = keycloakServerUrl + "/admin/realms/" + realm + "/users/" + keycloakUserId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(userUrl, HttpMethod.GET, request, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody();
        }

        throw new RuntimeException("Failed to get user info from Keycloak");
    }

    // Original methods remain the same
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
        return currentUser != null && currentUser.getId() != null && currentUser.getId().equals(userId);
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