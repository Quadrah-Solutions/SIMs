package com.quadrah.sims.service;

import com.quadrah.sims.model.UserAccount;
import com.quadrah.sims.repository.UserAccountRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final KeycloakService keycloakService;

    public UserAccountService(UserAccountRepository userAccountRepository, @Lazy KeycloakService keycloakService) {
        this.userAccountRepository = userAccountRepository;
        this.keycloakService = keycloakService;
    }

    public List<UserAccount> getAllUsers() {
        return userAccountRepository.findByIsActiveTrue();
    }

    public List<UserAccount> getUsersByRole(UserAccount.UserRole role) {
        return userAccountRepository.findByRoleAndIsActiveTrue(role);
    }

    public List<UserAccount> getNurses() {
        return userAccountRepository.findNurses();
    }

    public List<UserAccount> getTeachers() {
        return userAccountRepository.findTeachers();
    }

    public Optional<UserAccount> getUserById(Long id) {
        return userAccountRepository.findById(id);
    }

    public Optional<UserAccount> getUserByKeycloakId(String keycloakId) {
        return userAccountRepository.findByKeycloakId(keycloakId);
    }

    public Optional<UserAccount> getUserByUsername(String username) {
        return userAccountRepository.findByUsername(username);
    }

    public UserAccount getCurrentUser() {
        String keycloakId = keycloakService.getCurrentUserId();
        return userAccountRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalArgumentException("User not found in local database with Keycloak ID: " + keycloakId));
    }

    // ADDED: Create user method
    public UserAccount createUser(UserAccount user) {
        validateUser(user);

        // Check if Keycloak ID already exists
        if (user.getKeycloakId() != null && userAccountRepository.existsByKeycloakId(user.getKeycloakId())) {
            throw new IllegalArgumentException("User with Keycloak ID " + user.getKeycloakId() + " already exists.");
        }

        // Check if username already exists
        if (userAccountRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        user.setIsActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return userAccountRepository.save(user);
    }

    // ADDED: Update user method
    public UserAccount updateUser(Long id, UserAccount userDetails) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        validateUser(userDetails);

        // Check if username is being changed to an existing one
        if (!user.getUsername().equals(userDetails.getUsername()) &&
                userAccountRepository.existsByUsername(userDetails.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + userDetails.getUsername());
        }

        // Check if Keycloak ID is being changed to an existing one
        if (userDetails.getKeycloakId() != null &&
                !userDetails.getKeycloakId().equals(user.getKeycloakId()) &&
                userAccountRepository.existsByKeycloakId(userDetails.getKeycloakId())) {
            throw new IllegalArgumentException("Keycloak ID already exists: " + userDetails.getKeycloakId());
        }

        user.setKeycloakId(userDetails.getKeycloakId());
        user.setUsername(userDetails.getUsername());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());

        return userAccountRepository.save(user);
    }

    public UserAccount createOrUpdateUserFromKeycloak(String keycloakId, String username, String email,
                                                      String firstName, String lastName, UserAccount.UserRole role) {
        Optional<UserAccount> existingUser = userAccountRepository.findByKeycloakId(keycloakId);

        if (existingUser.isPresent()) {
            // Update existing user
            UserAccount user = existingUser.get();
            user.setUsername(username);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(role);
            user.setLastLogin(LocalDateTime.now());
            return userAccountRepository.save(user);
        } else {
            // Create new user
            UserAccount newUser = new UserAccount();
            newUser.setKeycloakId(keycloakId);
            newUser.setUsername(username);
            newUser.setEmail(email);
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setRole(role);
            newUser.setIsActive(true);
            newUser.setLastLogin(LocalDateTime.now());
            newUser.setCreatedAt(LocalDateTime.now());
            return userAccountRepository.save(newUser);
        }
    }

    public void recordLogin() {
        String keycloakId = keycloakService.getCurrentUserId();
        Optional<UserAccount> userOpt = userAccountRepository.findByKeycloakId(keycloakId);
        if (userOpt.isPresent()) {
            UserAccount user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userAccountRepository.save(user);
        }
    }

    public void deactivateUser(Long id) {
        UserAccount user = userAccountRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        user.setIsActive(false);
        userAccountRepository.save(user);
    }

    private void validateUser(UserAccount user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (user.getFirstName() == null || user.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (user.getLastName() == null || user.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (user.getRole() == null) {
            throw new IllegalArgumentException("User role is required.");
        }
    }
}