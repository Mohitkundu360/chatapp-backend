package com.example.chatapp.controller;

import com.example.chatapp.service.ChatRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatapp/chatrooms")
public class ChatRoomController {

    @Autowired
    private ChatRoomService chatRoomService;

    // ✅ Create Chat Room
    @PostMapping
    public ResponseEntity<?> createChatRoom(@RequestBody Map<String, String> request) {
        String roomName = request.get("roomName");
        return chatRoomService.createChatRoom(roomName);
    }

    // ✅ Join Chat Room
    @PostMapping("/{roomId}/join")
    public ResponseEntity<?> joinChatRoom(@PathVariable String roomId, @RequestBody Map<String, String> request) {
        String participant = request.get("participant");
        return chatRoomService.joinChatRoom(roomId, participant);
    }

    // ✅ Retrieve Chat History
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<?> getChatHistory(@PathVariable String roomId, @RequestParam(defaultValue = "10") int limit) {
        return chatRoomService.getChatHistory(roomId, limit);
    }

    // ✅ Send Message
    @PostMapping("/{roomId}/messages")
    public ResponseEntity<?> sendMessage(@PathVariable String roomId, @RequestBody Map<String, String> request) {
        String participant = request.get("participant");
        String message = request.get("message");
        return chatRoomService.sendMessage(roomId, participant, message);
    }
}