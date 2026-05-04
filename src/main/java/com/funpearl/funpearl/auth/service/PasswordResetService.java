package com.funpearl.funpearl.auth.service;

import com.funpearl.funpearl.auth.entity.PasswordResetToken;
import com.funpearl.funpearl.auth.repository.PasswordResetTokenRepository;
import com.funpearl.funpearl.exception.BadRequestException;
import com.funpearl.funpearl.exception.ResourceNotFoundException;
import com.funpearl.funpearl.user.entity.User;
import com.funpearl.funpearl.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration:3600000}")
    private long expirationMs;

    @Transactional
    public String createPasswordResetToken(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        // Delete existing unused tokens for this user
        tokenRepository.deleteByUserId(user.getId());

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(LocalDateTime.now().plusSeconds(expirationMs / 1000));
        resetToken.setUsed(false);

        tokenRepository.save(resetToken);

        // In production, send email here
        // emailService.sendPasswordResetEmail(user.getEmail(), token);

        return token;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

        if (resetToken.isExpired()) {
            throw new BadRequestException("Password reset token has expired");
        }

        if (resetToken.isUsed()) {
            throw new BadRequestException("Password reset token has already been used");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    public boolean isValidToken(String token) {
        return tokenRepository.findByToken(token)
                .map(t -> !t.isExpired() && !t.isUsed())
                .orElse(false);
    }
}
