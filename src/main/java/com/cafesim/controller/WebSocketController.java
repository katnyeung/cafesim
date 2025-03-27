package com.cafesim.controller;

import com.cafesim.model.ChatMessage;
import com.cafesim.model.User;
import com.cafesim.service.AIService;
import com.cafesim.service.ChatService;
import com.cafesim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private AIService aiService;

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage handleChat(ChatMessage message, Principal principal) {
        // Set the sender based on the authenticated user
        User user = userService.findByUsername(principal.getName());
        message.setSender(user);
        message.setAI(false);

        // Save the user message
        chatService.saveMessage(message);

        // Generate AI response
        String aiResponse = aiService.generateBartenderResponse(
                message.getContent(), user.getUsername());

        // Create and save the AI response
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setContent(aiResponse);
        aiMessage.setAI(true);
        chatService.saveMessage(aiMessage);

        // Return the original message (AI response will be sent separately)
        return message;
    }

    @MessageMapping("/seat-select")
    @SendTo("/topic/seat-updates")
    public Map<String, Object> handleSeatSelection(Map<String, Integer> request, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Integer seatNumber = request.get("seatNumber");

        // Handle seat selection logic
        boolean success = userService.assignSeat(user.getId(), seatNumber);

        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("seatNumber", seatNumber);
        response.put("username", user.getUsername());
        response.put("avatarUrl", user.getAvatarUrl());

        return response;
    }
}