package com.example.rebalance;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "application.kafka")
public record ApplicationKafkaProperties(Topic topic, Retry retry) {
    public record Topic(String source, String target) {}
    public record Retry(int attempts, long fixedDelay) {}
}
