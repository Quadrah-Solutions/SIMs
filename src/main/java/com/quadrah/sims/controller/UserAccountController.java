package com.quadrah.sims.controller;

import com.quadrah.sims.model.UserAccount;
import com.quadrah.sims.service.KeycloakService;
import com.quadrah.sims.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserAccountController {

    private final UserAccountService userService;
    private final KeycloakService keycloakService;

    public UserAccountController(UserAccountService userService, KeycloakService keycloakService) {
        this.userService = userService;
        this.keycloakService = keycloakService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')") // Only admins can list all users
    public ResponseEntity<List<UserAccount>> getAllUsers() {
        List<UserAccount> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @keycloakService.isCurrentUser(#id)")
    public ResponseEntity<UserAccount> getUserById(@PathVariable Long id) {
        Optional<UserAccount> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/username/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccount> getUserByUsername(@PathVariable String username) {
        Optional<UserAccount> user = userService.getUserByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/keycloak/{keycloakId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccount> getUserByKeycloakId(@PathVariable String keycloakId) {
        Optional<UserAccount> user = userService.getUserByKeycloakId(keycloakId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserAccount>> getUsersByRole(@PathVariable UserAccount.UserRole role) {
        List<UserAccount> users = userService.getUsersByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/nurses")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NURSE')")
    public ResponseEntity<List<UserAccount>> getNurses() {
        List<UserAccount> nurses = userService.getNurses();
        return ResponseEntity.ok(nurses);
    }

    @GetMapping("/teachers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('NURSE')")
    public ResponseEntity<List<UserAccount>> getTeachers() {
        List<UserAccount> teachers = userService.getTeachers();
        return ResponseEntity.ok(teachers);
    }

    @GetMapping("/current")
    public ResponseEntity<UserAccount> getCurrentUser() {
        UserAccount currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(currentUser);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAccount> createUser(@Valid @RequestBody UserAccount user) {
        // Note: User creation should primarily happen through Keycloak
        // This endpoint is for creating local user records after Keycloak user creation
        UserAccount createdUser = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PostMapping("/sync-keycloak")
    public ResponseEntity<UserAccount> syncWithKeycloak(@RequestBody SyncUserRequest syncRequest) {
        // This endpoint syncs a local user with Keycloak user data
        UserAccount syncedUser = userService.createOrUpdateUserFromKeycloak(
                syncRequest.getKeycloakId(),
                syncRequest.getUsername(),
                syncRequest.getEmail(),
                syncRequest.getFirstName(),
                syncRequest.getLastName(),
                syncRequest.getRole()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(syncedUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @keycloakService.isCurrentUser(#id)")
    public ResponseEntity<UserAccount> updateUser(@PathVariable Long id, @Valid @RequestBody UserAccount userDetails) {
        UserAccount updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/record-login")
    public ResponseEntity<Void> recordCurrentUserLogin() {
        userService.recordLogin();
        return ResponseEntity.ok().build();
    }

    // DTO classes for user synchronization
    public static class SyncUserRequest {
        private String keycloakId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private UserAccount.UserRole role;

        // Getters and setters
        public String getKeycloakId() { return keycloakId; }
        public void setKeycloakId(String keycloakId) { this.keycloakId = keycloakId; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public UserAccount.UserRole getRole() { return role; }
        public void setRole(UserAccount.UserRole role) { this.role = role; }
    }
}