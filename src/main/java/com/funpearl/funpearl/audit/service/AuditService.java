package com.funpearl.funpearl.audit.service;

import com.funpearl.funpearl.audit.entity.AuditLog;
import com.funpearl.funpearl.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    public void log(String action, String entityType, Long entityId, Long userId, 
                    String username, String ipAddress, String userAgent, 
                    String details, boolean success) {
        AuditLog auditLog = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .username(username)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .details(details)
                .success(success)
                .build();
        
        auditLogRepository.save(auditLog);
    }

    public void logLogin(String username, String ipAddress, String userAgent, boolean success, String details) {
        log("LOGIN", "USER", null, null, username, ipAddress, userAgent, details, success);
    }

    public void logLogout(Long userId, String username, String ipAddress) {
        log("LOGOUT", "USER", userId, userId, username, ipAddress, null, null, true);
    }

    public void logPasswordChange(Long userId, String username, String ipAddress, boolean success) {
        log("PASSWORD_CHANGE", "USER", userId, userId, username, ipAddress, null, null, success);
    }

    public void logPasswordReset(String email, String ipAddress, boolean success) {
        log("PASSWORD_RESET", "USER", null, null, email, ipAddress, null, null, success);
    }

    public void logProfileUpdate(Long userId, String username, String ipAddress, String details) {
        log("PROFILE_UPDATE", "USER", userId, userId, username, ipAddress, null, details, true);
    }

    public void logAccountLocked(String username, String ipAddress, String reason) {
        log("ACCOUNT_LOCKED", "USER", null, null, username, ipAddress, null, reason, true);
    }

    public List<AuditLog> getLogsByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId);
    }

    public Page<AuditLog> getLogsByUserId(Long userId, Pageable pageable) {
        return auditLogRepository.findByUserId(userId, pageable);
    }

    public List<AuditLog> getLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByCreatedAtBetween(start, end);
    }

    public List<AuditLog> getLogsByIpAddress(String ipAddress) {
        return auditLogRepository.findByIpAddress(ipAddress);
    }
}
