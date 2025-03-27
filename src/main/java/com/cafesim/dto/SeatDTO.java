package com.cafesim.dto;

import com.cafesim.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatDTO {
    private Long id;
    private Long roomId;
    private int position;
    private boolean occupied;
    private UserDTO user;

    // Convert Seat entity to SeatDTO
    public static SeatDTO fromEntity(Seat seat) {
        if (seat == null) {
            return null;
        }

        SeatDTO dto = new SeatDTO();
        dto.setId(seat.getId());
        dto.setRoomId(seat.getRoom().getId());
        dto.setPosition(seat.getPosition());
        dto.setOccupied(seat.isOccupied());

        if (seat.getUser() != null) {
            dto.setUser(UserDTO.fromEntity(seat.getUser()));
        }

        return dto;
    }
}