package com.funpearl.funpearl.user.controller;

import com.funpearl.funpearl.user.dto.ChangePasswordRequest;
import com.funpearl.funpearl.user.dto.UpdateProfileRequest;
import com.funpearl.funpearl.user.entity.User;
import com.funpearl.funpearl.security.CustomUserDetails;
import com.funpearl.funpearl.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userService.findById(userDetails.getId());
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        User updatedUser = userService.updateProfile(userDetails.getId(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getId(), request);
        return ResponseEntity.ok("Password changed successfully");
    }

    @DeleteMapping("/me")
    public ResponseEntity<String> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteAccount(userDetails.getId());
        return ResponseEntity.ok("Account deleted successfully");
    }
}
