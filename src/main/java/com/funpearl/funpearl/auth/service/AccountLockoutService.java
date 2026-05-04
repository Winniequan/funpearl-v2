package com.funpearl.funpearl.auth.service;

import com.funpearl.funpearl.user.entity.User;
import com.funpearl.funpearl.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountLockoutService {
    private final UserRepository userRepository;

    @Value("${app.lockout.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.lockout.duration-minutes:30}")
    private int lockoutDurationMinutes;

    public boolean isAccountLocked(User user) {
        if (user.getAccountLockedUntil() == null) {
            return false;
        }
        if (user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
            // Lock has expired, reset
            resetFailedAttempts(user);
            return false;
        }
        return true;
    }

    @Transactional
    public void recordFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxAttempts) {
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
        }

        userRepository.save(user);
    }

    @Transactional
    public void resetFailedAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLockedUntil(null);
        userRepository.save(user);
    }

    public int getRemainingAttempts(User user) {
        return Math.max(0, maxAttempts - user.getFailedLoginAttempts());
    }

    public long getLockoutTimeRemainingSeconds(User user) {
        if (user.getAccountLockedUntil() == null) {
            return 0;
        }
        if (user.getAccountLockedUntil().isBefore(LocalDateTime.now())) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), user.getAccountLockedUntil()).getSeconds();
    }
}
