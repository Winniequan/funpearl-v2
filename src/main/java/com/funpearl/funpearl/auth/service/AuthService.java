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

    public AuthResponse login(LoginRequest loginRequest) {
        // Check user exists and account status before authentication
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isEnabled()) {
            throw new UnauthorizedException("Account is disabled");
        }

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Please verify your email before logging in");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtService.generateToken(loginRequest.getUsername());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        return new AuthResponse(
                token,
                refreshToken.getToken(),
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
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
