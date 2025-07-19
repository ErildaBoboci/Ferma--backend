package com.eFarm.backend.repository;

import com.eFarm.backend.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByEmailAndTokenAndIsUsedFalse(String email, String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.email = :email AND p.isUsed = false")
    void deleteByEmailAndIsUsedFalse(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetToken p WHERE p.expiresAt < :now")
    void deleteByExpiresAtBefore(LocalDateTime now);

    boolean existsByEmailAndIsUsedFalse(String email);

    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.email = :email AND p.createdAt > :since")
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime since);

    @Query("SELECT COUNT(p) FROM PasswordResetToken p WHERE p.createdByIp = :ip AND p.createdAt > :since")
    long countByCreatedByIpAndCreatedAtAfter(String ip, LocalDateTime since);
}