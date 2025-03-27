package com.cafesim.controller;

import com.cafesim.model.ChatMessage;
import com.cafesim.model.Room;
import com.cafesim.model.User;
import com.cafesim.service.AIService;
import com.cafesim.service.ChatService;
import com.cafesim.service.RoomService;
import com.cafesim.service.UserService;
import com.cafesim.service.UserRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class WebSocketController {

    @Autowired
    private AIService aiService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRoomService userRoomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Handle chat messages in a specific room
     */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/room/{roomId}/messages")
    public ChatMessage handleChat(@DestinationVariable Long roomId, ChatMessage message, Principal principal) {
        // Set the sender based on the authenticated user
        User user = userService.findByUsername(principal.getName());
        message.setSender(user);
        message.setAI(false);

        // Set the room
        Optional<Room> roomOpt = roomService.getRoomById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            message.setRoom(room);

            // Update user activity in the room
            userRoomService.updateLastActive(user.getId(), roomId);

            // Save the user message
            ChatMessage savedMessage = chatService.saveMessage(message);

            // Generate AI response if the user is in the room
            if (userRoomService.isUserInRoom(user.getId(), roomId)) {
                String aiResponse = aiService.generateBartenderResponse(
                        message.getContent(), user.getUsername());

                // Create and save the AI response
                ChatMessage aiMessage = new ChatMessage();
                aiMessage.setContent(aiResponse);
                aiMessage.setAI(true);
                aiMessage.setRoom(room);

                ChatMessage savedAiMessage = chatService.saveMessage(aiMessage);

                // Send AI response to the room
                messagingTemplate.convertAndSend(
                        "/topic/room/" + roomId + "/messages",
                        savedAiMessage);
            }

            // Return the original user message
            return savedMessage;
        }

        return message;
    }

    /**
     * Handle seat selection in a room
     */
    @MessageMapping("/room/{roomId}/seat-select")
    @SendTo("/topic/room/{roomId}/seat-updates")
    public Map<String, Object> handleSeatSelection(
            @DestinationVariable Long roomId,
            Map<String, Integer> request,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());
        Integer seatPosition = request.get("seatPosition");

        // Ensure user has joined the room
        if (!userRoomService.isUserInRoom(user.getId(), roomId)) {
            userRoomService.addUserToRoom(user.getId(), roomId);
        }

        // Handle seat selection logic
        boolean success = userService.assignSeat(user.getId(), roomId, seatPosition);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("seatPosition", seatPosition);
        response.put("username", user.getUsername());
        response.put("userId", user.getId());
        response.put("avatarUrl", user.getAvatarUrl());

        return response;
    }

    /**
     * Handle user leaving a seat
     */
    @MessageMapping("/room/{roomId}/leave-seat")
    @SendTo("/topic/room/{roomId}/seat-updates")
    public Map<String, Object> handleLeaveSeat(
            @DestinationVariable Long roomId,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        // Get the current seat position before releasing it
        Optional<Integer> seatPosition = userService.findUserSeatPositionInRoom(user.getId(), roomId);

        // Release the seat
        userService.releaseSeat(user.getId(), roomId);

        Map<String, Object> response = new HashMap<>();
        response.put("action", "leave");
        response.put("success", true);
        response.put("username", user.getUsername());
        response.put("userId", user.getId());

        // Include the seat position if it was found
        seatPosition.ifPresent(pos -> response.put("seatPosition", pos));

        return response;
    }

    /**
     * Handle user joining a room
     */
    @MessageMapping("/join-room/{roomId}")
    @SendTo("/topic/room/{roomId}/user-updates")
    public Map<String, Object> handleJoinRoom(
            @DestinationVariable Long roomId,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        // Add user to room
        userRoomService.addUserToRoom(user.getId(), roomId);

        Map<String, Object> response = new HashMap<>();
        response.put("action", "join");
        response.put("username", user.getUsername());
        response.put("userId", user.getId());
        response.put("avatarUrl", user.getAvatarUrl());

        return response;
    }

    /**
     * Handle user leaving a room
     */
    @MessageMapping("/leave-room/{roomId}")
    @SendTo("/topic/room/{roomId}/user-updates")
    public Map<String, Object> handleLeaveRoom(
            @DestinationVariable Long roomId,
            Principal principal) {

        User user = userService.findByUsername(principal.getName());

        // Release any seat the user might have
        userService.releaseSeat(user.getId(), roomId);

        // Remove user from room
        userRoomService.removeUserFromRoom(user.getId(), roomId);

        Map<String, Object> response = new HashMap<>();
        response.put("action", "leave");
        response.put("username", user.getUsername());
        response.put("userId", user.getId());

        return response;
    }
}