package com.example.demo.controller;

import com.example.demo.service.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kafka")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.middleware.kafka.enabled", havingValue = "true")
public class KafkaController {

    private final KafkaProducerService kafkaProducerService;

    @PostMapping("/user-event")
    public ResponseEntity<String> sendUserEvent(@RequestParam String message) {
        kafkaProducerService.sendUserEvent(message);
        return ResponseEntity.ok("User event sent: " + message);
    }

    @PostMapping("/order-event")
    public ResponseEntity<String> sendOrderEvent(@RequestParam String message) {
        kafkaProducerService.sendOrderEvent(message);
        return ResponseEntity.ok("Order event sent: " + message);
    }

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(
            @RequestParam String topic,
            @RequestParam String key,
            @RequestParam String message) {
        kafkaProducerService.sendMessage(topic, key, message);
        return ResponseEntity.ok("Message sent to " + topic + ": " + message);
    }
}
