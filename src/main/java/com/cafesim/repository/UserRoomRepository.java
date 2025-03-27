package com.cafesim.repository;

import com.cafesim.model.UserRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {

    List<UserRoom> findByUserId(Long userId);

    List<UserRoom> findByRoomId(Long roomId);

    Optional<UserRoom> findByUserIdAndRoomId(Long userId, Long roomId);

    @Query("SELECT ur FROM UserRoom ur WHERE ur.user.id = :userId ORDER BY ur.lastActive DESC")
    List<UserRoom> findUserRoomsByUserIdOrderByLastActiveDesc(@Param("userId") Long userId);

    @Query("SELECT ur FROM UserRoom ur WHERE ur.room.id = :roomId ORDER BY ur.lastActive DESC")
    List<UserRoom> findUserRoomsByRoomIdOrderByLastActiveDesc(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(ur) FROM UserRoom ur WHERE ur.room.id = :roomId AND ur.lastActive > :since")
    Long countRecentActiveUsersInRoom(@Param("roomId") Long roomId, @Param("since") LocalDateTime since);

    @Query("SELECT DISTINCT ur.room.id FROM UserRoom ur WHERE ur.user.id = :userId")
    List<Long> findRoomIdsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndRoomId(Long userId, Long roomId);
}