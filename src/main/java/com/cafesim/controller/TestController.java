package com.cafesim.controller;

import com.cafesim.model.*;
import com.cafesim.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private AIService aiService;

    @Autowired
    private UserRoomService userRoomService;

    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> getStatus() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "running");
        status.put("message", "Café Simulation backend is operational");
        return ResponseEntity.ok(status);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllActiveRooms());
    }

    @PostMapping("/rooms")
    public ResponseEntity<Room> createRoom(@RequestParam String name,
                                           @RequestParam(required = false) String description,
                                           @RequestParam(defaultValue = "10") int capacity) {
        Room room = roomService.createRoom(name, description, capacity);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/rooms/{roomId}/seats")
    public ResponseEntity<List<Seat>> getRoomSeats(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getAvailableSeats(roomId));
    }

    @PostMapping("/users")
    public ResponseEntity<User> createTestUser(@RequestParam String username,
                                               @RequestParam String password,
                                               @RequestParam(required = false) String avatarDescription) {
        try {
            // For testing, use a simple avatar if none provided
            if (avatarDescription == null || avatarDescription.trim().isEmpty()) {
                avatarDescription = "a simple cartoon avatar";
            }

            User user = userService.registerUser(username, password, avatarDescription);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/users/{userId}/seat")
    public ResponseEntity<Map<String, Object>> assignSeat(@PathVariable Long userId,
                                                          @RequestParam Long roomId,
                                                          @RequestParam int seatPosition) {
        boolean success = userService.assignSeat(userId, roomId, seatPosition);

        Map<String, Object> result = new HashMap<>();
        result.put("success", success);

        if (success) {
            Optional<User> userOpt = userService.findById(userId);
            userOpt.ifPresent(user -> {
                result.put("username", user.getUsername());
                result.put("roomId", roomId);
                result.put("seatPosition", seatPosition);
            });
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> sendChatMessage(@RequestParam Long userId,
                                                               @RequestParam Long roomId,
                                                               @RequestParam String message) {
        Optional<User> userOpt = userService.findById(userId);
        Optional<Room> roomOpt = roomService.getRoomById(roomId);

        if (userOpt.isPresent() && roomOpt.isPresent()) {
            User user = userOpt.get();
            Room room = roomOpt.get();

            // Create and save user message
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setRoom(room);
            chatMessage.setSender(user);
            chatMessage.setContent(message);
            chatMessage.setAI(false);

            ChatMessage savedMessage = chatService.saveMessage(chatMessage);

            // Generate AI response
            String aiResponse = aiService.generateBartenderResponse(message, user.getUsername());

            // Create and save AI response
            ChatMessage aiMessage = new ChatMessage();
            aiMessage.setRoom(room);
            aiMessage.setContent(aiResponse);
            aiMessage.setAI(true);

            ChatMessage savedAiMessage = chatService.saveMessage(aiMessage);

            // Return both messages
            Map<String, Object> result = new HashMap<>();
            result.put("userMessage", savedMessage);
            result.put("aiResponse", savedAiMessage);

            return ResponseEntity.ok(result);
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getRoomMessages(@PathVariable Long roomId) {
        List<ChatMessage> messages = chatService.getRecentMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    // Multi-room functionality endpoints

    @PostMapping("/users/{userId}/join-room/{roomId}")
    public ResponseEntity<Map<String, Object>> joinRoom(@PathVariable Long userId, @PathVariable Long roomId) {
        try {
            UserRoom userRoom = userRoomService.addUserToRoom(userId, roomId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("userId", userId);
            result.put("roomId", roomId);
            result.put("joinedAt", userRoom.getJoinedAt());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @DeleteMapping("/users/{userId}/leave-room/{roomId}")
    public ResponseEntity<Map<String, Object>> leaveRoom(@PathVariable Long userId, @PathVariable Long roomId) {
        try {
            userRoomService.removeUserFromRoom(userId, roomId);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/users/{userId}/rooms")
    public ResponseEntity<List<Room>> getUserRooms(@PathVariable Long userId) {
        List<Room> rooms = userRoomService.getRoomsForUser(userId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/users/{userId}/recent-rooms")
    public ResponseEntity<List<Room>> getUserRecentRooms(@PathVariable Long userId) {
        List<Room> rooms = userRoomService.getRecentlyActiveRoomsForUser(userId);
        return ResponseEntity.ok(rooms);
    }

    @GetMapping("/rooms/{roomId}/users")
    public ResponseEntity<List<User>> getRoomUsers(@PathVariable Long roomId) {
        List<User> users = userRoomService.getUsersInRoom(roomId);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{userId}/favorite-room/{roomId}")
    public ResponseEntity<Map<String, Object>> favoriteRoom(
            @PathVariable Long userId,
            @PathVariable Long roomId,
            @RequestParam boolean favorite) {
        try {
            userRoomService.markRoomAsFavorite(userId, roomId, favorite);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(result);
        }
    }

    @GetMapping("/users/{userId}/favorite-rooms")
    public ResponseEntity<List<Room>> getUserFavoriteRooms(@PathVariable Long userId) {
        List<Room> rooms = userRoomService.getFavoriteRoomsForUser(userId);
        return ResponseEntity.ok(rooms);
    }
}