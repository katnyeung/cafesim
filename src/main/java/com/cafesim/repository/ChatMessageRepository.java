package com.cafesim.repository;

import com.cafesim.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId ORDER BY m.timestamp DESC LIMIT :limit")
    List<ChatMessage> findRecentByRoomId(@Param("roomId") Long roomId, @Param("limit") int limit);

    List<ChatMessage> findByRoomIdOrderByTimestampDesc(Long roomId);
}