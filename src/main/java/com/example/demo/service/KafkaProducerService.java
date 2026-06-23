package com.example.demo.service;

import com.example.demo.config.KafkaConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.middleware.kafka.enabled", havingValue = "true")
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendUserEvent(String message) {
        kafkaTemplate.send(KafkaConfig.TOPIC_USER_EVENTS, message);
    }

    public void sendOrderEvent(String message) {
        kafkaTemplate.send(KafkaConfig.TOPIC_ORDER_EVENTS, message);
    }

    public void sendMessage(String topic, String key, String message) {
        kafkaTemplate.send(topic, key, message);
    }

    public CompletableFuture<SendResult<String, String>> sendAsync(String topic, String message) {
        return kafkaTemplate.send(topic, message);
    }
}
