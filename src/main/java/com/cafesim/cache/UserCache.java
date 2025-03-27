package com.cafesim.cache;

import com.cafesim.model.User;
import com.cafesim.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserCache {

    @Autowired
    private CacheService cacheService;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final long USER_CACHE_DURATION = 30; // 30 minutes

    // Cache user data
    public void cacheUser(User user) {
        if (user != null && user.getId() != null) {
            String key = USER_CACHE_PREFIX + user.getId();
            cacheService.put(key, user, USER_CACHE_DURATION);

            // Also cache by username for quick lookups
            String usernameKey = USER_CACHE_PREFIX + "name:" + user.getUsername();
            cacheService.put(usernameKey, user.getId(), USER_CACHE_DURATION);
        }
    }

    // Get cached user by ID
    public User getUserById(Long userId) {
        if (userId != null) {
            String key = USER_CACHE_PREFIX + userId;
            return cacheService.get(key, User.class);
        }
        return null;
    }

    // Get cached user by username
    public User getUserByUsername(String username) {
        if (username != null) {
            String usernameKey = USER_CACHE_PREFIX + "name:" + username;
            Long userId = cacheService.get(usernameKey, Long.class);

            if (userId != null) {
                return getUserById(userId);
            }
        }
        return null;
    }

    // Invalidate user cache
    public void invalidateUser(User user) {
        if (user != null) {
            if (user.getId() != null) {
                String key = USER_CACHE_PREFIX + user.getId();
                cacheService.delete(key);
            }

            if (user.getUsername() != null) {
                String usernameKey = USER_CACHE_PREFIX + "name:" + user.getUsername();
                cacheService.delete(usernameKey);
            }
        }
    }
}