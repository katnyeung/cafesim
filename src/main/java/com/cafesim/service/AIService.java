package com.cafesim.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";

    public String generateBartenderResponse(String userMessage, String username) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4o");

        // Create a system message for the bartender persona
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a friendly AI bartender in a virtual café. Respond in a conversational, helpful manner. " +
                "Keep responses brief (1-3 sentences). You can serve virtual drinks and chat about casual topics. " +
                "You can acknowledge other users in the café if they are mentioned.");

        // Create the user message
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", username + ": " + userMessage);

        // Add both messages to the requestBody
        requestBody.put("messages", List.of(systemMessage, userMsg));
        requestBody.put("max_tokens", 150);
        requestBody.put("temperature", 0.7);

        // Set headers for the request
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Create the HTTP entity with headers and body
        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

        // Send the request and get the response
        org.springframework.http.ResponseEntity<Map> response =
                restTemplate.postForEntity(OPENAI_API_URL, entity, Map.class);

        // Extract the AI's response
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
        Map<String, Object> choice = choices.get(0);
        Map<String, String> message = (Map<String, String>) choice.get("message");

        return message.get("content");
    }
}