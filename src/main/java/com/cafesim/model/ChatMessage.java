package com.cafesim.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    private Room room;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    @ToString.Exclude
    private User sender;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_ai")
    private boolean isAI;

    @Column
    private LocalDateTime timestamp = LocalDateTime.now();

    // Constructor for creating chat messages
    public ChatMessage(Room room, User sender, String content, boolean isAI) {
        this.room = room;
        this.sender = sender;
        this.content = content;
        this.isAI = isAI;
        this.timestamp = LocalDateTime.now();
    }
}