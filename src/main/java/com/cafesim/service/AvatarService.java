package com.cafesim.service;

import com.cafesim.model.AvatarRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AvatarService {

    @Value("${openai.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    private static final String DALLE_API_URL = "https://api.openai.com/v1/images/generations";

    public String generateAvatar(String description) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "dall-e-3");
        requestBody.put("prompt", "A simple, cartoon-style avatar of " + description +
                ". The avatar should be a head and shoulders portrait with a neutral background. " +
                "Make it appropriate for all ages and suitable for a café game.");
        requestBody.put("n", 1);
        requestBody.put("size", "256x256");

        // Set headers for the request
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        // Create the HTTP entity with headers and body
        org.springframework.http.HttpEntity<Map<String, Object>> entity =
                new org.springframework.http.HttpEntity<>(requestBody, headers);

        // Send the request and get the response
        org.springframework.http.ResponseEntity<Map> response =
                restTemplate.postForEntity(DALLE_API_URL, entity, Map.class);

        // Extract the image URL
        Map<String, Object> responseBody = response.getBody();
        List<Map<String, String>> data = (List<Map<String, String>>) responseBody.get("data");

        return data.get(0).get("url");
    }
}