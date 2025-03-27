package com.cafesim.dto;

import com.cafesim.model.Room;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {
    private Long id;
    private String name;
    private String description;
    private int capacity;
    private boolean active;
    private LocalDateTime createdAt;
    private int totalSeats;
    private int availableSeats;
    private int occupiedSeats;
    private boolean isFavorite;

    // Convert Room entity to RoomDTO
    public static RoomDTO fromEntity(Room room) {
        if (room == null) {
            return null;
        }

        RoomDTO dto = new RoomDTO();
        dto.setId(room.getId());
        dto.setName(room.getName());
        dto.setDescription(room.getDescription());
        dto.setCapacity(room.getCapacity());
        dto.setActive(room.isActive());
        dto.setCreatedAt(room.getCreatedAt());

        // Compute seat information
        if (room.getSeats() != null) {
            dto.setTotalSeats(room.getSeats().size());
            dto.setOccupiedSeats((int) room.getSeats().stream().filter(seat -> seat.isOccupied()).count());
            dto.setAvailableSeats(dto.getTotalSeats() - dto.getOccupiedSeats());
        }

        return dto;
    }

    // Convert Room entity to RoomDTO with favorite information
    public static RoomDTO fromEntityWithFavorite(Room room, boolean isFavorite) {
        RoomDTO dto = fromEntity(room);
        if (dto != null) {
            dto.setFavorite(isFavorite);
        }
        return dto;
    }
}