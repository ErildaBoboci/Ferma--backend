package com.eFarm.backend.repository;

import com.eFarm.backend.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByToken(String token);

    Optional<EmailVerificationToken> findByEmailAndTokenAndIsUsedFalse(String email, String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken e WHERE e.email = :email AND e.isUsed = false")
    void deleteByEmailAndIsUsedFalse(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM EmailVerificationToken e WHERE e.expiresAt < :now")
    void deleteByExpiresAtBefore(LocalDateTime now);

    boolean existsByEmailAndIsUsedFalse(String email);

    @Query("SELECT COUNT(e) FROM EmailVerificationToken e WHERE e.email = :email AND e.createdAt > :since")
    long countByEmailAndCreatedAtAfter(String email, LocalDateTime since);
}