package com.funpearl.funpearl.auth.repository;

import com.funpearl.funpearl.auth.entity.EmailVerificationToken;
import com.funpearl.funpearl.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    Optional<EmailVerificationToken> findByToken(String token);
    Optional<EmailVerificationToken> findByUser(User user);
    void deleteByUser(User user);
}
