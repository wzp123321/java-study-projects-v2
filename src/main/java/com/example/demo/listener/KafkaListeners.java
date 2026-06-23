package com.example.demo.listener;

import com.example.demo.config.KafkaConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.middleware.kafka.enabled", havingValue = "true")
public class KafkaListeners {

    @KafkaListener(topics = KafkaConfig.TOPIC_USER_EVENTS, groupId = KafkaConfig.GROUP_ID)
    public void handleUserEvents(String message) {
        log.info("收到用户事件: {}", message);
    }

    @KafkaListener(topics = KafkaConfig.TOPIC_ORDER_EVENTS, groupId = KafkaConfig.GROUP_ID)
    public void handleOrderEvents(String message) {
        log.info("收到订单事件: {}", message);
    }
}
