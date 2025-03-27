package com.cafesim.service;

import com.cafesim.cache.ChatCache;
import com.cafesim.model.ChatMessage;
import com.cafesim.model.Room;
import com.cafesim.repository.ChatMessageRepository;
import com.cafesim.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ChatCache chatCache;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Save a chat message and cache it
     */
    public ChatMessage saveMessage(ChatMessage message) {
        // If room is null, use the default room
        if (message.getRoom() == null) {
            List<Room> rooms = roomRepository.findByActiveTrue();
            if (!rooms.isEmpty()) {
                message.setRoom(rooms.get(0));
            } else {
                // Create a default room if none exists
                Room defaultRoom = createDefaultRoom();
                message.setRoom(defaultRoom);
            }
        }

        // Save to database
        ChatMessage savedMessage = chatMessageRepository.save(message);

        // Cache the message
        chatCache.addMessage(message.getRoom().getId(), savedMessage);

        // Send to websocket
        messagingTemplate.convertAndSend("/topic/messages", savedMessage);

        return savedMessage;
    }

    /**
     * Get recent messages for a room
     */
    public List<ChatMessage> getRecentMessages(Long roomId) {
        // Try to get from cache first
        List<ChatMessage> cachedMessages = chatCache.getRecentMessages(roomId);
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            return cachedMessages;
        }

        // If not in cache, get from database
        return chatMessageRepository.findRecentByRoomId(roomId, 50);
    }

    /**
     * Create a default room if none exists
     */
    private Room createDefaultRoom() {
        Optional<Room> defaultRoomOpt = roomRepository.findById(1L);
        if (defaultRoomOpt.isPresent()) {
            return defaultRoomOpt.get();
        } else {
            Room defaultRoom = new Room("Default Room", "The default café room", 10);
            defaultRoom.initializeSeats();
            return roomRepository.save(defaultRoom);
        }
    }
}