package com.cafesim.repository;

import com.cafesim.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    List<Room> findByActiveTrue();

    @Query("SELECT r FROM Room r WHERE r.active = true AND " +
            "(SELECT COUNT(s) FROM Seat s WHERE s.room = r AND s.occupied = false) > 0")
    List<Room> findAvailableRooms();

    Room findByName(String name);
}