package com.cafesim.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_rooms", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "room_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    private Room room;

    @Column(name = "joined_at")
    private LocalDateTime joinedAt = LocalDateTime.now();

    @Column(name = "last_active")
    private LocalDateTime lastActive = LocalDateTime.now();

    @Column
    private boolean favorite = false;

    // Constructor for joining a room
    public UserRoom(User user, Room room) {
        this.user = user;
        this.room = room;
        this.joinedAt = LocalDateTime.now();
        this.lastActive = LocalDateTime.now();
    }
}