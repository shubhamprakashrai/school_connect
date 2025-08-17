package com.schoolmgmt.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Service for managing blacklisted JWT tokens.
 * Uses Redis to store blacklisted tokens until they expire.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        String key = BLACKLIST_PREFIX + token;
        
        // Store token in Redis with TTL equal to token expiration
        redisTemplate.opsForValue().set(
            key, 
            "blacklisted", 
            jwtExpiration, 
            TimeUnit.MILLISECONDS
        );
        
        log.debug("Token blacklisted: {}", token.substring(0, Math.min(token.length(), 20)) + "...");
    }
    
    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        String key = BLACKLIST_PREFIX + token;
        Boolean exists = redisTemplate.hasKey(key);
        
        return exists != null && exists;
    }
    
    /**
     * Remove token from blacklist (if needed for testing or admin purposes)
     */
    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }
        
        String key = BLACKLIST_PREFIX + token;
        redisTemplate.delete(key);
        
        log.debug("Token removed from blacklist");
    }
    
    /**
     * Clear all blacklisted tokens (use with caution)
     */
    public void clearAllBlacklistedTokens() {
        var keys = redisTemplate.keys(BLACKLIST_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Cleared {} blacklisted tokens", keys.size());
        }
    }
}
