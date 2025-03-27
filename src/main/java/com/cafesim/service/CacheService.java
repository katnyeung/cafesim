package com.cafesim.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long DEFAULT_EXPIRATION = 60; // 1 hour in minutes

    // Store an object in cache
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, DEFAULT_EXPIRATION, TimeUnit.MINUTES);
    }

    // Store an object with custom expiration time
    public void put(String key, Object value, long expirationInMinutes) {
        redisTemplate.opsForValue().set(key, value, expirationInMinutes, TimeUnit.MINUTES);
    }

    // Retrieve an object from cache
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }

    // Check if a key exists in cache
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // Remove a key from cache
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    // Store in hash
    public void hashPut(String key, String hashKey, Object value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
        redisTemplate.expire(key, DEFAULT_EXPIRATION, TimeUnit.MINUTES);
    }

    // Get from hash
    @SuppressWarnings("unchecked")
    public <T> T hashGet(String key, String hashKey, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, hashKey);
        if (value != null && clazz.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
}