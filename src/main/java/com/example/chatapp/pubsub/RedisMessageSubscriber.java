package com.example.chatapp.pubsub;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Service
public class RedisMessageSubscriber implements MessageListener {

    @Override
    public void onMessage(@NonNull Message message, @Nullable byte[] pattern) {
        System.out.println("🔴 Received message: " + message.toString());
    }
}
