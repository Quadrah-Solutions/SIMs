package com.quadrah.sims.repository;

import com.quadrah.sims.model.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    // Find user by username
    Optional<UserAccount> findByUsername(String username);

    // Find users by role
    List<UserAccount> findByRole(UserAccount.UserRole role);

    // Find by keycloak Id
    Optional<UserAccount> findByKeycloakId(String keycloakId);

    // Find active users by role
    List<UserAccount> findByRoleAndIsActiveTrue(UserAccount.UserRole role);

    // Check if username exists (for validation)
    boolean existsByUsername(String username);

    // Find all active users
    List<UserAccount> findByIsActiveTrue();

    // Find users by name search
    @Query("SELECT u FROM UserAccount u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :name, '%')) OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<UserAccount> findByNameContainingIgnoreCase(String name);

    // Find nurses (common query)
    default List<UserAccount> findNurses() {
        return findByRoleAndIsActiveTrue(UserAccount.UserRole.NURSE);
    }

    // Find teachers (common query)
    default List<UserAccount> findTeachers() {
        return findByRoleAndIsActiveTrue(UserAccount.UserRole.TEACHER);
    }

    boolean existsByKeycloakId(String keycloakId);

}
