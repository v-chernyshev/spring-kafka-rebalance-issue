package com.example.rebalance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.EndpointHandlerMethod;
import org.springframework.stereotype.Component;

@Component
public class DeadLetterTopicProcessor {
    private static final Logger logger = LoggerFactory.getLogger(DeadLetterTopicProcessor.class);

    public static final EndpointHandlerMethod ENDPOINT_HANDLER_METHOD =
            new EndpointHandlerMethod(DeadLetterTopicProcessor.class, "processMessage");

    public void processMessage(String message) {
        logger.error("Failed to process message=\"{}\"", message);
    }
}
