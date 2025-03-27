package com.cafesim.repository;

import com.cafesim.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByRoomId(Long roomId);

    List<Seat> findByRoomIdAndOccupied(Long roomId, boolean occupied);

    Optional<Seat> findByRoomIdAndPosition(Long roomId, int position);

    Optional<Seat> findByUserUsername(String username);

    List<Seat> findByUserIdAndOccupied(Long userId, boolean occupied);

    @Query("SELECT s FROM Seat s WHERE s.user.id = :userId AND s.room.id = :roomId")
    Optional<Seat> findByUserIdAndRoomId(@Param("userId") Long userId, @Param("roomId") Long roomId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.room.id = :roomId AND s.occupied = true")
    Long countOccupiedSeatsInRoom(@Param("roomId") Long roomId);

    @Query("SELECT s FROM Seat s WHERE s.user.id = :userId")
    List<Seat> findAllByUserId(@Param("userId") Long userId);
}
