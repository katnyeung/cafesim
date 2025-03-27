package com.cafesim.config;

import com.cafesim.model.Room;
import com.cafesim.model.User;
import com.cafesim.service.RoomService;
import com.cafesim.service.UserRoomService;
import com.cafesim.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRoomService userRoomService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${cafe.default-room.name:Main Lounge}")
    private String defaultRoomName;

    @Value("${cafe.default-room.description:A cozy lounge for casual conversations}")
    private String defaultRoomDescription;

    @Value("${cafe.default-room.capacity:15}")
    private int defaultRoomCapacity;

    /**
     * Initialize default data for demo/development environments
     */
    @Bean
    @Profile({"dev", "demo"})
    public CommandLineRunner initializeData() {
        return args -> {
            // Create default rooms
            createDefaultRooms();

            // Create test user if none exists
            createTestUserIfNone();
        };
    }

    private void createDefaultRooms() {
        // Check if default room exists
        if (roomService.getRoomByName(defaultRoomName).isEmpty()) {
            Room mainRoom = roomService.createRoom(
                    defaultRoomName,
                    defaultRoomDescription,
                    defaultRoomCapacity
            );
            System.out.println("Created default room: " + mainRoom.getName());
        }

        // Create additional themed rooms if none exist
        if (roomService.getAllActiveRooms().size() <= 1) {
            roomService.createRoom(
                    "Coffee Corner",
                    "A quiet nook perfect for those who love coffee and reading",
                    8
            );

            roomService.createRoom(
                    "Jazz Bar",
                    "A lively space with jazz music playing in the background",
                    12
            );

            roomService.createRoom(
                    "Garden Terrace",
                    "An outdoor terrace with plants and natural lighting",
                    10
            );

            System.out.println("Created additional themed rooms");
        }
    }

    private void createTestUserIfNone() {
        // Check if test user exists
        if (userService.findByUsername("testuser") == null) {
            try {
                // Create a test user
                User testUser = userService.registerUser(
                        "testuser",
                        "password123",
                        "a friendly cartoon person with a coffee mug"
                );

                System.out.println("Created test user: " + testUser.getUsername());

                // Add the test user to all rooms
                roomService.getAllActiveRooms().forEach(room -> {
                    userRoomService.addUserToRoom(testUser.getId(), room.getId());
                    System.out.println("Added test user to room: " + room.getName());
                });

                // Favorite the first room
                if (!roomService.getAllActiveRooms().isEmpty()) {
                    Room firstRoom = roomService.getAllActiveRooms().get(0);
                    userRoomService.markRoomAsFavorite(testUser.getId(), firstRoom.getId(), true);
                    System.out.println("Marked room as favorite: " + firstRoom.getName());
                }
            } catch (Exception e) {
                System.err.println("Failed to create test user: " + e.getMessage());
            }
        }
    }
}