package com.funpearl.funpearl.user.service;

import com.funpearl.funpearl.auth.service.EmailVerificationService;
import com.funpearl.funpearl.exception.BadRequestException;
import com.funpearl.funpearl.exception.ResourceNotFoundException;
import com.funpearl.funpearl.user.dto.ChangePasswordRequest;
import com.funpearl.funpearl.user.dto.UpdateProfileRequest;
import com.funpearl.funpearl.user.entity.Role;
import com.funpearl.funpearl.user.entity.User;
import com.funpearl.funpearl.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User updateProfile(Long userId, UpdateProfileRequest request) {
        User user = findById(userId);

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (existsByUsername(request.getUsername())) {
                throw new BadRequestException("Username already taken");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
            user.setEmailVerified(false);
        }

        User savedUser = userRepository.save(user);

        // Create new verification token if email changed
        if (!savedUser.isEmailVerified()) {
            emailVerificationService.createVerificationToken(savedUser);
        }

        return savedUser;
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = findById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    // Admin methods
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Page<User> findAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Transactional
    public User assignRole(Long userId, Role role) {
        User user = findById(userId);
        user.getRoles().add(role);
        return userRepository.save(user);
    }

    @Transactional
    public User removeRole(Long userId, Role role) {
        User user = findById(userId);
        user.getRoles().remove(role);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }
}
