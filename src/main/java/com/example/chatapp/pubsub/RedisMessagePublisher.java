package com.example.chatapp.pubsub;

public interface RedisMessagePublisher {
    void publish(String channel, String message);
}