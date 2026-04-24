package com.funpearl.funpearl.auth.service;

import com.funpearl.funpearl.auth.entity.EmailVerificationToken;
import com.funpearl.funpearl.auth.repository.EmailVerificationTokenRepository;
import com.funpearl.funpearl.exception.BadRequestException;
import com.funpearl.funpearl.exception.ResourceNotFoundException;
import com.funpearl.funpearl.user.entity.User;
import com.funpearl.funpearl.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;

    @Value("${app.email.verification-expiration:86400000}")
    private long verificationExpirationMs;

    @Transactional
    public EmailVerificationToken createVerificationToken(User user) {
        tokenRepository.findByUser(user).ifPresent(tokenRepository::delete);
        
        EmailVerificationToken token = new EmailVerificationToken(user, verificationExpirationMs);
        return tokenRepository.save(token);
    }

    @Transactional
    public void verifyEmail(String token) {
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            tokenRepository.delete(verificationToken);
            throw new BadRequestException("Verification token has expired. Please request a new one.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(verificationToken);
    }

    @Transactional
    public EmailVerificationToken resendVerificationToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.isEmailVerified()) {
            throw new BadRequestException("Email is already verified");
        }

        return createVerificationToken(user);
    }
}
