package com.schoolmgmt.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class TokenBlacklistService {

    private final Set<String> blacklist = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public void blacklistToken(String token) {
        if (token == null || token.isEmpty()) return;
        blacklist.add(token);
        log.debug("Token blacklisted");
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null || token.isEmpty()) return false;
        return blacklist.contains(token);
    }

    public void removeFromBlacklist(String token) {
        if (token == null || token.isEmpty()) return;
        blacklist.remove(token);
    }

    public void clearAllBlacklistedTokens() {
        int size = blacklist.size();
        blacklist.clear();
        log.info("Cleared {} blacklisted tokens", size);
    }
}
