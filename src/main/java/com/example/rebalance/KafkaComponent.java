package com.example.rebalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.TransientDataAccessResourceException;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Component;

@Component
public class KafkaComponent {
    private static final Logger logger = LoggerFactory.getLogger(KafkaComponent.class);

    @KafkaListener(topics = "#{topicProperties.source}")
    @SendTo("#{topicProperties.target}")
    public String processMessage(String message) {
        logger.info("Processing message=\"{}\"", message);
        throw new TransientDataAccessResourceException("Simulating a transient failure");
    }

    @DltHandler
    public void processDeadLetterMessage(String message) {
        logger.error("Failed to process message=\"{}\"", message);
    }
}
