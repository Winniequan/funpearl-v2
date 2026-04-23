package com.funpearl.funpearl.admin.controller;

import com.funpearl.funpearl.admin.dto.AssignRoleRequest;
import com.funpearl.funpearl.admin.dto.UserResponse;
import com.funpearl.funpearl.user.entity.User;
import com.funpearl.funpearl.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAllUsers().stream()
                .map(UserResponse::fromUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/paged")
    public ResponseEntity<Page<UserResponse>> getAllUsersPaged(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserResponse> users = userService.findAllUsers(pageable)
                .map(UserResponse::fromUser);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<UserResponse> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {
        User user = userService.assignRole(userId, request.getRole());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    @DeleteMapping("/users/{userId}/roles")
    public ResponseEntity<UserResponse> removeRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {
        User user = userService.removeRole(userId, request.getRole());
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }
}
