package com.funpearl.funpearl.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {
    
    @Value("${app.rate-limit.max-attempts:5}")
    private int maxAttempts;
    
    @Value("${app.rate-limit.window-minutes:15}")
    private int windowMinutes;
    
    private final Map<String, RateLimitInfo> attemptCache = new ConcurrentHashMap<>();
    
    public boolean isBlocked(String key) {
        RateLimitInfo info = attemptCache.get(key);
        if (info == null) {
            return false;
        }
        
        // Reset if window has passed
        if (info.windowStart.plusMinutes(windowMinutes).isBefore(LocalDateTime.now())) {
            attemptCache.remove(key);
            return false;
        }
        
        return info.attempts >= maxAttempts;
    }
    
    public void recordAttempt(String key) {
        attemptCache.compute(key, (k, info) -> {
            if (info == null || info.windowStart.plusMinutes(windowMinutes).isBefore(LocalDateTime.now())) {
                return new RateLimitInfo(1, LocalDateTime.now());
            }
            info.attempts++;
            return info;
        });
    }
    
    public void resetAttempts(String key) {
        attemptCache.remove(key);
    }
    
    public int getRemainingAttempts(String key) {
        RateLimitInfo info = attemptCache.get(key);
        if (info == null) {
            return maxAttempts;
        }
        if (info.windowStart.plusMinutes(windowMinutes).isBefore(LocalDateTime.now())) {
            return maxAttempts;
        }
        return Math.max(0, maxAttempts - info.attempts);
    }
    
    public long getBlockTimeRemainingSeconds(String key) {
        RateLimitInfo info = attemptCache.get(key);
        if (info == null) {
            return 0;
        }
        LocalDateTime unblockTime = info.windowStart.plusMinutes(windowMinutes);
        if (unblockTime.isBefore(LocalDateTime.now())) {
            return 0;
        }
        return java.time.Duration.between(LocalDateTime.now(), unblockTime).getSeconds();
    }
    
    private static class RateLimitInfo {
        int attempts;
        LocalDateTime windowStart;
        
        RateLimitInfo(int attempts, LocalDateTime windowStart) {
            this.attempts = attempts;
            this.windowStart = windowStart;
        }
    }
}
