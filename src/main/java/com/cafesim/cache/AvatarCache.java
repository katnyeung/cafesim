package com.cafesim.cache;

import com.cafesim.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AvatarCache {

    @Autowired
    private CacheService cacheService;

    private static final String AVATAR_CACHE_PREFIX = "avatar:";
    private static final long AVATAR_CACHE_DURATION = 1440; // 24 hours in minutes

    // Cache avatar URL for a user
    public void cacheAvatarUrl(String username, String avatarUrl) {
        String key = AVATAR_CACHE_PREFIX + username;
        cacheService.put(key, avatarUrl, AVATAR_CACHE_DURATION);
    }

    // Get cached avatar URL for a user
    public String getAvatarUrl(String username) {
        String key = AVATAR_CACHE_PREFIX + username;
        return cacheService.get(key, String.class);
    }

    // Check if avatar URL is cached
    public boolean hasAvatarUrl(String username) {
        String key = AVATAR_CACHE_PREFIX + username;
        return cacheService.hasKey(key);
    }

    // Invalidate cached avatar URL
    public void invalidateAvatarUrl(String username) {
        String key = AVATAR_CACHE_PREFIX + username;
        cacheService.delete(key);
    }
}