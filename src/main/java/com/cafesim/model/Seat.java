package com.cafesim.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Table(name = "seats", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"room_id", "position"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    private Room room;

    @Column(nullable = false)
    private int position;

    @Column
    private boolean occupied = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @ToString.Exclude
    private User user;

    // Constructor for creating seats
    public Seat(Room room, int position) {
        this.room = room;
        this.position = position;
        this.occupied = false;
    }

    // Custom setter for user that also updates the occupied status
    public void setUser(User user) {
        this.user = user;
        this.occupied = user != null;
    }
}