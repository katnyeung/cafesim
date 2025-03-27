package com.cafesim.service;

import com.cafesim.cache.UserCache;
import com.cafesim.model.Room;
import com.cafesim.model.Seat;
import com.cafesim.model.User;
import com.cafesim.repository.RoomRepository;
import com.cafesim.repository.SeatRepository;
import com.cafesim.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AvatarService avatarService;

    @Autowired
    private UserCache userCache;

    /**
     * Find a user by username
     */
    public User findByUsername(String username) {
        // First check cache
        User cachedUser = userCache.getUserByUsername(username);
        if (cachedUser != null) {
            return cachedUser;
        }

        // Cache miss - fetch from database
        User user = userRepository.findByUsername(username);

        // Cache for future requests
        if (user != null) {
            userCache.cacheUser(user);
        }

        return user;
    }

    /**
     * Find a user by ID
     */
    public Optional<User> findById(Long id) {
        // First check cache
        User cachedUser = userCache.getUserById(id);
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        }

        // Cache miss - fetch from database
        Optional<User> userOpt = userRepository.findById(id);

        // Cache for future requests
        userOpt.ifPresent(userCache::cacheUser);

        return userOpt;
    }

    /**
     * Register a new user
     */
    @Transactional
    public User registerUser(String username, String password, String avatarDescription) {
        // Check if username already exists
        if (userRepository.findByUsername(username) != null) {
            throw new RuntimeException("Username already exists");
        }

        // Generate avatar using OpenAI DALL-E
        String avatarUrl = avatarService.generateAvatar(avatarDescription);

        // Create and save user
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAvatarDescription(avatarDescription);
        user.setAvatarUrl(avatarUrl);
        user.setCreatedAt(LocalDateTime.now());
        user.setLastLogin(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        // Cache the new user
        userCache.cacheUser(savedUser);

        return savedUser;
    }

    /**
     * Update user login timestamp
     */
    @Transactional
    public void updateLastLogin(Long userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setLastLogin(LocalDateTime.now());
            userRepository.save(user);

            // Update user in cache
            userCache.cacheUser(user);
        }
    }

    /**
     * Update user avatar
     */
    @Transactional
    public User updateAvatar(Long userId, String avatarDescription) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Generate new avatar
            String avatarUrl = avatarService.generateAvatar(avatarDescription);

            user.setAvatarDescription(avatarDescription);
            user.setAvatarUrl(avatarUrl);

            User updatedUser = userRepository.save(user);

            // Update user in cache
            userCache.cacheUser(updatedUser);

            return updatedUser;
        }
        throw new RuntimeException("User not found");
    }

    /**
     * Assign a seat to a user in a specific room
     */
    @Transactional
    public boolean assignSeat(Long userId, Long roomId, int seatPosition) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Room> roomOpt = roomRepository.findById(roomId);

        if (userOpt.isPresent() && roomOpt.isPresent()) {
            User user = userOpt.get();
            Room room = roomOpt.get();

            // First release any existing seat for this user in this room
            releaseUserSeatsInRoom(userId, roomId);

            // Find the requested seat
            Optional<Seat> seatOpt = seatRepository.findByRoomIdAndPosition(roomId, seatPosition);

            if (seatOpt.isPresent()) {
                Seat seat = seatOpt.get();

                // Check if seat is available
                if (!seat.isOccupied()) {
                    seat.setUser(user);
                    seat.setOccupied(true);
                    seatRepository.save(seat);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Release all seats assigned to a user in a specific room
     */
    @Transactional
    public void releaseUserSeatsInRoom(Long userId, Long roomId) {
        seatRepository.findByRoomIdAndOccupied(roomId, true).stream()
                .filter(seat -> seat.getUser() != null && seat.getUser().getId().equals(userId))
                .forEach(seat -> {
                    seat.setUser(null);
                    seat.setOccupied(false);
                    seatRepository.save(seat);
                });
    }

    /**
     * Release a specific seat for a user
     */
    @Transactional
    public void releaseSeat(Long userId, Long roomId) {
        findUserSeatInRoom(userId, roomId).ifPresent(seat -> {
            seat.setUser(null);
            seat.setOccupied(false);
            seatRepository.save(seat);
        });
    }

    /**
     * Find a user's seat in a specific room
     */
    public Optional<Seat> findUserSeatInRoom(Long userId, Long roomId) {
        return seatRepository.findByRoomIdAndOccupied(roomId, true).stream()
                .filter(seat -> seat.getUser() != null && seat.getUser().getId().equals(userId))
                .findFirst();
    }

    /**
     * Check if a user is seated in a specific room
     */
    public boolean isUserSeatedInRoom(Long userId, Long roomId) {
        return findUserSeatInRoom(userId, roomId).isPresent();
    }

    /**
     * Get all seated users in a room
     */
    public List<User> getSeatedUsersInRoom(Long roomId) {
        return seatRepository.findByRoomIdAndOccupied(roomId, true).stream()
                .map(Seat::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Check if a user is in a specific room
     */
    public boolean isUserInRoom(Long userId, Long roomId) {
        return seatRepository.findByRoomIdAndOccupied(roomId, true).stream()
                .anyMatch(seat -> seat.getUser() != null && seat.getUser().getId().equals(userId));
    }

    /**
     * Get a list of rooms where a user is seated
     */
    public List<Room> getRoomsForUser(Long userId) {
        return seatRepository.findByUserIdAndOccupied(userId, true).stream()
                .map(Seat::getRoom)
                .collect(Collectors.toList());
    }
}