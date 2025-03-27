package com.cafesim.controller;

import com.cafesim.dto.ApiResponse;
import com.cafesim.dto.RoomDTO;
import com.cafesim.dto.RoomRequest;
import com.cafesim.dto.SeatDTO;
import com.cafesim.model.Room;
import com.cafesim.model.Seat;
import com.cafesim.service.RoomService;
import com.cafesim.service.UserRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserRoomService userRoomService;

    @PostMapping
    public ResponseEntity<ApiResponse<RoomDTO>> createRoom(@Valid @RequestBody RoomRequest request) {
        Room room = roomService.createRoom(
                request.getName(),
                request.getDescription(),
                request.getCapacity()
        );

        return ResponseEntity.ok(ApiResponse.success(
                RoomDTO.fromEntity(room),
                "Room created successfully"
        ));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getAllActiveRooms() {
        List<RoomDTO> rooms = roomService.getAllActiveRooms().stream()
                .map(RoomDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                rooms,
                "Retrieved " + rooms.size() + " active rooms"
        ));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<RoomDTO>>> getAvailableRooms() {
        List<RoomDTO> rooms = roomService.getAvailableRooms().stream()
                .map(RoomDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                rooms,
                "Retrieved " + rooms.size() + " available rooms"
        ));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomDTO>> getRoomById(@PathVariable Long roomId) {
        Optional<Room> roomOpt = roomService.getRoomById(roomId);

        return roomOpt.map(room -> ResponseEntity.ok(ApiResponse.success(
                RoomDTO.fromEntity(room),
                "Room retrieved successfully"
        ))).orElseGet(() -> ResponseEntity.ok(ApiResponse.error("Room not found")));
    }

    @GetMapping("/{roomId}/seats")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getAvailableSeats(@PathVariable Long roomId) {
        List<SeatDTO> seats = roomService.getAvailableSeats(roomId).stream()
                .map(SeatDTO::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(
                seats,
                "Retrieved " + seats.size() + " available seats"
        ));
    }

    @GetMapping("/{roomId}/allSeats")
    public ResponseEntity<ApiResponse<List<SeatDTO>>> getAllSeats(@PathVariable Long roomId) {
        Optional<Room> roomOpt = roomService.getRoomById(roomId);

        if (roomOpt.isPresent()) {
            List<SeatDTO> seats = roomOpt.get().getSeats().stream()
                    .map(SeatDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(ApiResponse.success(
                    seats,
                    "Retrieved all seats for room"
            ));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Room not found"));
        }
    }

    @PutMapping("/{roomId}/deactivate")
    public ResponseEntity<ApiResponse<Boolean>> deactivateRoom(@PathVariable Long roomId) {
        boolean success = roomService.deactivateRoom(roomId);

        if (success) {
            return ResponseEntity.ok(ApiResponse.success(
                    true,
                    "Room deactivated successfully"
            ));
        } else {
            return ResponseEntity.ok(ApiResponse.error("Failed to deactivate room"));
        }
    }

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<Boolean>> joinRoom(
            @PathVariable Long roomId,
            Authentication authentication) {

        // Get user ID from authentication
        Long userId = getUserIdFromAuthentication(authentication);

        try {
            userRoomService.addUserToRoom(userId, roomId);
            return ResponseEntity.ok(ApiResponse.success(
                    true,
                    "Successfully joined room"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Failed to join room: " + e.getMessage()
            ));
        }
    }

    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Boolean>> leaveRoom(
            @PathVariable Long roomId,
            Authentication authentication) {

        // Get user ID from authentication
        Long userId = getUserIdFromAuthentication(authentication);

        try {
            userRoomService.removeUserFromRoom(userId, roomId);
            return ResponseEntity.ok(ApiResponse.success(
                    true,
                    "Successfully left room"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.error(
                    "Failed to leave room: " + e.getMessage()
            ));
        }
    }

    // Helper method to extract user ID from authentication
    private Long getUserIdFromAuthentication(Authentication authentication) {
        // This is a simplified implementation
        // In a real application, you would extract the user ID from your UserDetails or JWT token
        return 1L; // Placeholder - replace with actual implementation
    }