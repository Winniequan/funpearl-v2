package com.funpearl.funpearl.auth.service;

import com.funpearl.funpearl.security.jwt.JwtService;
import com.funpearl.funpearl.auth.dto.AuthResponse;
import com.funpearl.funpearl.auth.dto.SignupRequest;
import com.funpearl.funpearl.auth.dto.SignupResponse;
import com.funpearl.funpearl.auth.dto.LoginRequest;
import com.funpearl.funpearl.auth.dto.RefreshTokenRequest;
import com.funpearl.funpearl.auth.dto.TokenRefreshResponse;
import com.funpearl.funpearl.auth.entity.RefreshToken;
import com.funpearl.funpearl.exception.BadRequestException;
import com.funpearl.funpearl.exception.ResourceNotFoundException;
import com.funpearl.funpearl.exception.UnauthorizedException;
import com.funpearl.funpearl.user.entity.User;
import com.funpearl.funpearl.user.repository.UserRepository;
import com.funpearl.funpearl.audit.service.AuditService;
import com.funpearl.funpearl.security.RateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final AccountLockoutService accountLockoutService;
    private final AuditService auditService;
    private final RateLimiterService rateLimiterService;

    /**
     * Register
     */
    @Transactional
    public SignupResponse register(SignupRequest signupRequest) {
        // check user exists or not
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            throw new BadRequestException("Username already exists");
        }

        // check email exists or not
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User newUser = new User();
        newUser.setUsername(signupRequest.getUsername());
        newUser.setEmail(signupRequest.getEmail());
        newUser.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        User savedUser = userRepository.save(newUser);

        // Create email verification token
        emailVerificationService.createVerificationToken(savedUser);

        return new SignupResponse(
                "Registration successful. Please check your email to verify your account.",
                savedUser.getEmail()
        );
    }

    /**
     * user login
     */

    public AuthResponse login(LoginRequest loginRequest, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        // Check rate limiting by IP
        if (rateLimiterService.isBlocked(ipAddress)) {
            long remainingSeconds = rateLimiterService.getBlockTimeRemainingSeconds(ipAddress);
            auditService.logLogin(loginRequest.getUsername(), ipAddress, userAgent, false, "Rate limited");
            throw new UnauthorizedException("Too many login attempts. Please try again in " + remainingSeconds + " seconds");
        }

        // Check user exists and account status before authentication
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> {
                    rateLimiterService.recordAttempt(ipAddress);
                    auditService.logLogin(loginRequest.getUsername(), ipAddress, userAgent, false, "User not found");
                    return new ResourceNotFoundException("Invalid credentials");
                });

        // Check account lockout
        if (accountLockoutService.isAccountLocked(user)) {
            long remainingSeconds = accountLockoutService.getLockoutTimeRemainingSeconds(user);
            auditService.logLogin(loginRequest.getUsername(), ipAddress, userAgent, false, "Account locked");
            throw new UnauthorizedException("Account is locked. Please try again in " + remainingSeconds + " seconds");
        }

        if (!user.isEnabled()) {
            auditService.logLogin(loginRequest.getUsername(), ipAddress, userAgent, false, "Account disabled");
            throw new UnauthorizedException("Account is disabled");
        }

        if (!user.isEmailVerified()) {
            auditService.logLogin(loginRequest.getUsername(), ipAddress, userAgent, false, "Email not verified");
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Reset failed attempts on successful login
            accountLockoutService.resetFailedAttempts(user);
            rateLimiterService.resetAttempts(ipAddress);

            String token = jwtService.generateToken(loginRequest.getUsername());
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

            auditService.logLogin(loginRequest.getUsername(), ipAddress, userAgent, true, null);

            return new AuthResponse(
                    token,
                    refreshToken.getToken(),
                    user.getId(),
                    user.getUsername(),
                    user.getEmail()
            );
        } catch (Exception e) {
            // Record failed attempt
            rateLimiterService.recordAttempt(ipAddress);
            accountLockoutService.recordFailedAttempt(user);

            if (accountLockoutService.isAccountLocked(user)) {
                auditService.logAccountLocked(loginRequest.getUsername(), ipAddress, "Max failed attempts reached");
            }

            auditService.logLogin(loginRequest.getUsername(), ipAddress, userAgent, false, "Invalid password");
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public TokenRefreshResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken());
        refreshTokenService.verifyExpiration(refreshToken);

        User user = refreshToken.getUser();
        String newAccessToken = jwtService.generateToken(user.getUsername());

        return new TokenRefreshResponse(newAccessToken, refreshToken.getToken());
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteByUserId(userId);
    }
}
