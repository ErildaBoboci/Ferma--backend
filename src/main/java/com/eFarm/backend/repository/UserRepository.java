package com.eFarm.backend.repository;

import com.eFarm.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailVerificationToken(String token);

    Optional<User> findByPasswordResetToken(String token);

    @Query("SELECT u FROM User u WHERE u.role.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);

    @Query("SELECT u FROM User u WHERE u.isEnabled = true")
    List<User> findAllEnabledUsers();

    @Query("SELECT u FROM User u WHERE u.isEmailVerified = false")
    List<User> findAllUnverifiedUsers();

    @Query("SELECT u FROM User u WHERE u.emailVerificationExpiresAt < :now")
    List<User> findUsersWithExpiredVerificationTokens(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.passwordResetExpiresAt < :now")
    List<User> findUsersWithExpiredPasswordResetTokens(@Param("now") LocalDateTime now);

    @Query("SELECT u FROM User u WHERE u.firstName LIKE %:name% OR u.lastName LIKE %:name% OR u.email LIKE %:name%")
    List<User> findByNameOrEmail(@Param("name") String name);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name = :roleName")
    long countByRoleName(@Param("roleName") String roleName);

    @Query("SELECT COUNT(u) FROM User u WHERE u.isEnabled = true")
    long countEnabledUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate")
    long countUsersCreatedAfter(@Param("startDate") LocalDateTime startDate);
}