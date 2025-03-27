package com.cafesim.repository;

import com.cafesim.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.lastLogin > :since ORDER BY u.lastLogin DESC")
    List<User> findRecentlyActiveUsers(@Param("since") LocalDateTime since);

    @Query("SELECT u FROM User u JOIN Seat s ON u.id = s.user.id WHERE s.room.id = :roomId")
    List<User> findUsersInRoom(@Param("roomId") Long roomId);

    @Query("SELECT COUNT(u) FROM User u JOIN Seat s ON u.id = s.user.id WHERE s.room.id = :roomId")
    Long countUsersInRoom(@Param("roomId") Long roomId);
}