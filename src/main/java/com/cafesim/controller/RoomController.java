package com.cafesim.controller;

import com.cafesim.dto.RoomRequest;
import com.cafesim.model.Room;
import com.cafesim.model.Seat;
import com.cafesim.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    public ResponseEntity<Room> createRoom(@RequestBody RoomRequest request) {
        Room room = roomService.createRoom(
                request.getName(),
                request.getDescription(),
                request.getCapacity()
        );
        return ResponseEntity.ok(room);
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllActiveRooms() {
        return ResponseEntity.ok(roomService.getAllActiveRooms());
    }

    @GetMapping("/available")
    public ResponseEntity<List<Room>> getAvailableRooms() {
        return ResponseEntity.ok(roomService.getAvailableRooms());
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Room> getRoomById(@PathVariable Long roomId) {
        Optional<Room> roomOpt = roomService.getRoomById(roomId);
        return roomOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{roomId}/seats")
    public ResponseEntity<List<Seat>> getAvailableSeats(@PathVariable Long roomId) {
        return ResponseEntity.ok(roomService.getAvailableSeats(roomId));
    }

    @PutMapping("/{roomId}/deactivate")
    public ResponseEntity<Boolean> deactivateRoom(@PathVariable Long roomId) {
        boolean success = roomService.deactivateRoom(roomId);
        if (success) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
