package com.cafesim.cache;

import com.cafesim.model.ChatMessage;
import com.cafesim.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ChatCache {

    @Autowired
    private CacheService cacheService;

    private static final String CHAT_CACHE_PREFIX = "chat:room:";
    private static final String RECENT_MESSAGES_KEY = "recent_messages";
    private static final int MAX_CACHED_MESSAGES = 50;
    private static final long CHAT_CACHE_DURATION = 60; // 1 hour in minutes

    // Add a message to the room's recent messages cache
    public void addMessage(Long roomId, ChatMessage message) {
        String key = CHAT_CACHE_PREFIX + roomId;

        // Get the list of recent messages or create a new one
        List<ChatMessage> messages = getRecentMessages(roomId);
        if (messages == null) {
            messages = new ArrayList<>();
        }

        // Add the new message and trim if needed
        messages.add(message);
        if (messages.size() > MAX_CACHED_MESSAGES) {
            messages = messages.subList(messages.size() - MAX_CACHED_MESSAGES, messages.size());
        }

        // Update the cache
        cacheService.hashPut(key, RECENT_MESSAGES_KEY, messages);

        // Set expiration time
        cacheService.put(key, "placeholder", CHAT_CACHE_DURATION);
    }

    // Get recent messages for a room
    @SuppressWarnings("unchecked")
    public List<ChatMessage> getRecentMessages(Long roomId) {
        String key = CHAT_CACHE_PREFIX + roomId;
        return cacheService.hashGet(key, RECENT_MESSAGES_KEY, List.class);
    }

    // Clear recent messages for a room
    public void clearRecentMessages(Long roomId) {
        String key = CHAT_CACHE_PREFIX + roomId;
        cacheService.delete(key);
    }
}