package com.quadrah.sims.service;

import com.quadrah.sims.dto.UserDTO;
import com.quadrah.sims.dto.UserResponse;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KeycloakUserService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    public KeycloakUserService(Keycloak keycloak) {
        this.keycloak = keycloak;
    }

    public UserResponse createUser(UserDTO request) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Create user representation
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(false);

        // Set password credential
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false); // User will not be forced to change password on first login

        user.setCredentials(Collections.singletonList(credential));

        // Create user in Keycloak
        Response response = usersResource.create(user);

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo().getReasonPhrase());
        }

        // Get the created user ID from location header
        String userId = getCreatedId(response);

        // Assign role to the user
        assignRoleToUser(userId, request.getRole());

        // Return user response
        return getUserById(userId);
    }

    public List<UserResponse> getAllUsers() {
        RealmResource realmResource = keycloak.realm(realm);
        List<UserRepresentation> users = realmResource.users().list();

        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(String userId) {
        RealmResource realmResource = keycloak.realm(realm);
        UserRepresentation user = realmResource.users().get(userId).toRepresentation();
        return mapToUserResponse(user);
    }

    private void assignRoleToUser(String userId, String roleName) {
        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        // Get realm role
        var realmRole = realmResource.roles().get(roleName).toRepresentation();

        // Assign role to user
        usersResource.get(userId).roles().realmLevel().add(Collections.singletonList(realmRole));
    }

    private UserResponse mapToUserResponse(UserRepresentation user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setStatus(user.isEnabled() ? "Active" : "Inactive");

        // Get user roles
        List<String> roles = getUserRoles(user.getId());
        if (!roles.isEmpty()) {
            response.setRole(roles.get(0)); // Get first role for simplicity
        }

        return response;
    }

    private List<String> getUserRoles(String userId) {
        RealmResource realmResource = keycloak.realm(realm);
        var roles = realmResource.users().get(userId).roles().realmLevel().listAll();

        return roles.stream()
                .map(role -> role.getName())
                .collect(Collectors.toList());
    }

    private String getCreatedId(Response response) {
        String location = response.getLocation().toString();
        return location.substring(location.lastIndexOf("/") + 1);
    }
}
