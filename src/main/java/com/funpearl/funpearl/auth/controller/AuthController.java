package com.funpearl.funpearl.auth.controller;

import com.funpearl.funpearl.auth.dto.AuthResponse;
import com.funpearl.funpearl.auth.dto.RefreshTokenRequest;
import com.funpearl.funpearl.auth.dto.SignupResponse;
import com.funpearl.funpearl.auth.dto.TokenRefreshResponse;
import com.funpearl.funpearl.auth.service.AuthService;
import com.funpearl.funpearl.auth.service.EmailVerificationService;
import com.funpearl.funpearl.auth.dto.SignupRequest;
import com.funpearl.funpearl.auth.dto.LoginRequest;
import com.funpearl.funpearl.security.CustomUserDetails;

import com.funpearl.funpearl.auth.dto.ForgotPasswordRequest;
import com.funpearl.funpearl.auth.dto.ResetPasswordRequest;
import com.funpearl.funpearl.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AuthController {
    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/signup")
    public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.register(signupRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        return authService.login(loginRequest, request);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetToken(request.getEmail());
        return ResponseEntity.ok("Password reset email sent. Please check your inbox.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Password reset successfully");
    }

    @PostMapping("/refresh")
    public TokenRefreshResponse refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return authService.refreshToken(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getId());
        return ResponseEntity.ok("Logged out successfully");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerification(@AuthenticationPrincipal CustomUserDetails userDetails) {
        emailVerificationService.resendVerificationToken(userDetails.getId());
        return ResponseEntity.ok("Verification email sent");
    }
}