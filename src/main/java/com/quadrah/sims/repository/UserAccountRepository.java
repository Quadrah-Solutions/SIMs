package com.quadrah.sims.repository;

import com.quadrah.sims.model.UserAccount;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    // Find user by username
    Optional<UserAccount> findByUsername(String username);

    // Find users by role
    List<UserAccount> findByRole(UserAccount.UserRole role);

    // Find users by multiple roles
    List<UserAccount> findByRoleIn(List<UserAccount.UserRole> roles);

    // Find active users by multiple roles
    List<UserAccount> findByRoleInAndIsActiveTrue(List<UserAccount.UserRole> roles);

    // Find by keycloak Id
    Optional<UserAccount> findByKeycloakId(String keycloakId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserAccount u WHERE u.keycloakId = :keycloakId")
    Optional<UserAccount> findWithLockingByKeycloakId(String keycloakId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserAccount u WHERE u.username = :username")
    Optional<UserAccount> findWithLockingByUsername(String username);

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

    // Find admins (common query)
    default List<UserAccount> findAdmins() {
        return findByRoleAndIsActiveTrue(UserAccount.UserRole.ADMIN);
    }

    // Find nurses and admins (common query for notifications)
    default List<UserAccount> findNursesAndAdmins() {
        return findByRoleInAndIsActiveTrue(List.of(UserAccount.UserRole.NURSE, UserAccount.UserRole.ADMIN));
    }

    boolean existsByKeycloakId(String keycloakId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserAccount u WHERE u.keycloakId = :keycloakId")
    Optional<UserAccount> findByKeycloakIdWithLock(@Param("keycloakId") String keycloakId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserAccount u WHERE u.username = :username")
    Optional<UserAccount> findByUsernameWithLock(@Param("username") String username);

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO user_accounts (keycloak_id, username, email, first_name, last_name, 
                                  role, is_active, created_at, last_login) 
        VALUES (:keycloakId, :username, :email, :firstName, :lastName, 
                CAST(:role AS text), true, NOW(), NOW())
        ON CONFLICT (keycloak_id) 
        DO UPDATE SET 
            email = EXCLUDED.email,
            first_name = EXCLUDED.first_name,
            last_name = EXCLUDED.last_name,
            role = CAST(EXCLUDED.role AS text),
            last_login = EXCLUDED.last_login,
            username = COALESCE(NULLIF(user_accounts.username, ''), EXCLUDED.username)
        """, nativeQuery = true)
    void upsertUserNative(
            @Param("keycloakId") String keycloakId,
            @Param("username") String username,
            @Param("email") String email,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("role") String role);
}