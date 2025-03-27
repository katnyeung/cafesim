package com.cafesim.service;

import com.cafesim.model.Room;
import com.cafesim.model.User;
import com.cafesim.model.UserRoom;
import com.cafesim.repository.RoomRepository;
import com.cafesim.repository.UserRepository;
import com.cafesim.repository.UserRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserRoomService {

    @Autowired
    private UserRoomRepository userRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    /**
     * Add a user to a room
     */
    @Transactional
    public UserRoom addUserToRoom(Long userId, Long roomId) {
        // Check if the user is already in the room
        Optional<UserRoom> existingUserRoom = userRoomRepository.findByUserIdAndRoomId(userId, roomId);
        if (existingUserRoom.isPresent()) {
            UserRoom userRoom = existingUserRoom.get();
            userRoom.setLastActive(LocalDateTime.now());
            return userRoomRepository.save(userRoom);
        }

        // Find the user and room
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Room> roomOpt = roomRepository.findById(roomId);

        if (userOpt.isPresent() && roomOpt.isPresent()) {
            User user = userOpt.get();
            Room room = roomOpt.get();

            // Create a new UserRoom relationship
            UserRoom userRoom = new UserRoom(user, room);
            return userRoomRepository.save(userRoom);
        }

        throw new RuntimeException("User or Room not found");
    }

    /**
     * Remove a user from a room
     */
    @Transactional
    public void removeUserFromRoom(Long userId, Long roomId) {
        Optional<UserRoom> userRoomOpt = userRoomRepository.findByUserIdAndRoomId(userId, roomId);
        userRoomOpt.ifPresent(userRoomRepository::delete);
    }

    /**
     * Get all rooms a user has joined
     */
    public List<Room> getRoomsForUser(Long userId) {
        return userRoomRepository.findByUserId(userId).stream()
                .map(UserRoom::getRoom)
                .collect(Collectors.toList());
    }

    /**
     * Get recently active rooms for a user
     */
    public List<Room> getRecentlyActiveRoomsForUser(Long userId) {
        return userRoomRepository.findUserRoomsByUserIdOrderByLastActiveDesc(userId).stream()
                .map(UserRoom::getRoom)
                .collect(Collectors.toList());
    }

    /**
     * Get all users in a room
     */
    public List<User> getUsersInRoom(Long roomId) {
        return userRoomRepository.findByRoomId(roomId).stream()
                .map(UserRoom::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Mark a room as favorite for a user
     */
    @Transactional
    public void markRoomAsFavorite(Long userId, Long roomId, boolean favorite) {
        Optional<UserRoom> userRoomOpt = userRoomRepository.findByUserIdAndRoomId(userId, roomId);

        if (userRoomOpt.isPresent()) {
            UserRoom userRoom = userRoomOpt.get();
            userRoom.setFavorite(favorite);
            userRoomRepository.save(userRoom);
        } else {
            throw new RuntimeException("User is not in this room");
        }
    }

    /**
     * Get favorite rooms for a user
     */
    public List<Room> getFavoriteRoomsForUser(Long userId) {
        return userRoomRepository.findByUserId(userId).stream()
                .filter(UserRoom::isFavorite)
                .map(UserRoom::getRoom)
                .collect(Collectors.toList());
    }

    /**
     * Update last active timestamp for a user in a room
     */
    @Transactional
    public void updateLastActive(Long userId, Long roomId) {
        Optional<UserRoom> userRoomOpt = userRoomRepository.findByUserIdAndRoomId(userId, roomId);

        if (userRoomOpt.isPresent()) {
            UserRoom userRoom = userRoomOpt.get();
            userRoom.setLastActive(LocalDateTime.now());
            userRoomRepository.save(userRoom);
        }
    }

    /**
     * Check if a user is in a room
     */
    public boolean isUserInRoom(Long userId, Long roomId) {
        return userRoomRepository.existsByUserIdAndRoomId(userId, roomId);
    }

    /**
     * Check if a room is marked as favorite for a user
     */
    public boolean isRoomFavorite(Long userId, Long roomId) {
        Optional<UserRoom> userRoomOpt = userRoomRepository.findByUserIdAndRoomId(userId, roomId);
        return userRoomOpt.map(UserRoom::isFavorite).orElse(false);
    }

    /**
     * Get a list of users who are currently active in a room
     */
    public List<User> getActiveUsersInRoom(Long roomId, int minutesThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);

        return userRoomRepository.findByRoomId(roomId).stream()
                .filter(userRoom -> userRoom.getLastActive().isAfter(threshold))
                .map(UserRoom::getUser)
                .collect(Collectors.toList());
    }

    /**
     * Get a count of active users in a room
     */
    public long countActiveUsersInRoom(Long roomId, int minutesThreshold) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(minutesThreshold);
        return userRoomRepository.countRecentActiveUsersInRoom(roomId, threshold);
    }

    /**
     * Get the timestamp when a user last visited a room
     */
    public Optional<LocalDateTime> getLastActiveTime(Long userId, Long roomId) {
        Optional<UserRoom> userRoomOpt = userRoomRepository.findByUserIdAndRoomId(userId, roomId);
        return userRoomOpt.map(UserRoom::getLastActive);
    }
}