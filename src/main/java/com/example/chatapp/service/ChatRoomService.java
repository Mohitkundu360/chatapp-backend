package com.example.chatapp.service;

import com.example.chatapp.pubsub.RedisMessagePublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

@Service
public class ChatRoomService {

    private static final String CHAT_ROOMS_KEY = "CHAT_ROOMS";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisMessagePublisher redisMessagePublisher;

    /**
     * Create a chat room.
     */
    public ResponseEntity<?> createChatRoom(String roomName) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomName);
        if (roomExists != null && roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomName + "' already exists.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Create the chat room in Redis
        redisTemplate.opsForHash().put(CHAT_ROOMS_KEY, roomName, roomName);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Chat room '" + roomName + "' created successfully.");
        response.put("roomId", roomName);
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Join a chat room.
     */
    public ResponseEntity<?> joinChatRoom(String roomId, String participant) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomId);
        if (roomExists == null || !roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomId + "' does not exist.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Add participant to the Redis Set for this room
        String participantsKey = "CHAT_ROOM:" + roomId + ":PARTICIPANTS";
        redisTemplate.opsForSet().add(participantsKey, participant);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User '" + participant + "' joined chat room '" + roomId + "'.");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve chat history for a chat room.
     */
    @SuppressWarnings("unchecked")
    public ResponseEntity<?> getChatHistory(String roomId, int limit) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomId);
        if (roomExists == null || !roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomId + "' does not exist.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        String messagesKey = "CHAT_ROOM:" + roomId + ":MESSAGES";
        List<Object> messagesFromRedis = redisTemplate.opsForList().range(messagesKey, -limit, -1);

        List<Map<String, Object>> messages = new ArrayList<>();
        if (messagesFromRedis != null) {
            for (Object obj : messagesFromRedis) {
                messages.add((Map<String, Object>) obj);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("messages", messages);

        return ResponseEntity.ok(response);
    }

    /**
     * Send a message to the chat room and publish via Redis Pub/Sub.
     */
    public ResponseEntity<?> sendMessage(String roomId, String participant, String message) {
        Boolean roomExists = redisTemplate.opsForHash().hasKey(CHAT_ROOMS_KEY, roomId);
        if (roomExists == null || !roomExists) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Chat room '" + roomId + "' does not exist.");
            response.put("status", "error");
            return ResponseEntity.badRequest().body(response);
        }

        // Save the message to Redis List
        String messagesKey = "CHAT_ROOM:" + roomId + ":MESSAGES";
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("participant", participant);
        messageData.put("message", message);
        messageData.put("timestamp", java.time.Instant.now().toString());

        redisTemplate.opsForList().rightPush(messagesKey, messageData);

        // Publish the message to Redis channel for real-time delivery
        redisMessagePublisher.publish("chatroom", participant + ": " + message);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Message sent successfully.");
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }
}