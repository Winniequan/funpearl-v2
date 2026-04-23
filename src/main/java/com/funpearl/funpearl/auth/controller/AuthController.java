package com.funpearl.funpearl.auth.controller;

import com.funpearl.funpearl.auth.dto.AuthResponse;
import com.funpearl.funpearl.auth.dto.RefreshTokenRequest;
import com.funpearl.funpearl.auth.dto.TokenRefreshResponse;
import com.funpearl.funpearl.auth.service.AuthService;
import com.funpearl.funpearl.auth.dto.SignupRequest;
import com.funpearl.funpearl.auth.dto.LoginRequest;
import com.funpearl.funpearl.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public AuthResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
        return authService.register(signupRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
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
}