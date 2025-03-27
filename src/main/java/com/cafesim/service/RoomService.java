package com.cafesim.service;


import com.cafesim.model.Room;
import com.cafesim.model.Seat;
import com.cafesim.repository.RoomRepository;
import com.cafesim.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private SeatRepository seatRepository;

    /**
     * Create a new room with the specified capacity
     */
    @Transactional
    public Room createRoom(String name, String description, int capacity) {
        Room room = new Room(name, description, capacity);
        room = roomRepository.save(room);

        // Initialize seats for this room
        room.initializeSeats();
        room = roomRepository.save(room);

        return room;
    }

    /**
     * Get all active rooms
     */
    public List<Room> getAllActiveRooms() {
        return roomRepository.findByActiveTrue();
    }

    /**
     * Get rooms with available seats
     */
    public List<Room> getAvailableRooms() {
        return roomRepository.findAvailableRooms();
    }

    /**
     * Get a room by ID
     */
    public Optional<Room> getRoomById(Long roomId) {
        return roomRepository.findById(roomId);
    }

    /**
     * Deactivate a room
     */
    @Transactional
    public boolean deactivateRoom(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            room.setActive(false);
            roomRepository.save(room);
            return true;
        }
        return false;
    }

    /**
     * Get available seats in a room
     */
    public List<Seat> getAvailableSeats(Long roomId) {
        Optional<Room> roomOpt = roomRepository.findById(roomId);
        if (roomOpt.isPresent()) {
            Room room = roomOpt.get();
            return room.getSeats().stream()
                    .filter(seat -> !seat.isOccupied())
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    /**
     * Check if a seat is available in a room
     */
    public boolean isSeatAvailable(Long roomId, int position) {
        Optional<Seat> seatOpt = seatRepository.findByRoomIdAndPosition(roomId, position);
        return seatOpt.map(seat -> !seat.isOccupied()).orElse(false);
    }
}