package com.cafesim.dto;

import com.cafesim.model.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    private Long id;
    private Long roomId;
    private UserDTO sender;
    private String content;
    private boolean isAI;
    private LocalDateTime timestamp;

    // Convert ChatMessage entity to ChatMessageDTO
    public static ChatMessageDTO fromEntity(ChatMessage message) {
        if (message == null) {
            return null;
        }

        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(message.getId());

        if (message.getRoom() != null) {
            dto.setRoomId(message.getRoom().getId());
        }

        if (message.getSender() != null) {
            dto.setSender(UserDTO.fromEntity(message.getSender()));
        }

        dto.setContent(message.getContent());
        dto.setAI(message.isAI());
        dto.setTimestamp(message.getTimestamp());

        return dto;
    }
}