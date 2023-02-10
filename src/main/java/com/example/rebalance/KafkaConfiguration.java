package com.example.rebalance;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.common.serialization.VoidDeserializer;
import org.apache.kafka.common.serialization.VoidSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.retrytopic.RetryTopicConfiguration;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationBuilder;
import org.springframework.kafka.retrytopic.RetryTopicConfigurationSupport;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableConfigurationProperties(ApplicationKafkaProperties.class)
public class KafkaConfiguration extends RetryTopicConfigurationSupport {
    private final KafkaProperties springProperties;
    private final ApplicationKafkaProperties applicationProperties;

    public KafkaConfiguration(KafkaProperties springProperties, ApplicationKafkaProperties applicationProperties) {
        this.springProperties = springProperties;
        this.applicationProperties = applicationProperties;
    }

    @Bean
    ApplicationKafkaProperties.Topic topicProperties() {
        return this.applicationProperties.topic();
    }

    @Bean
    ConsumerFactory<Void, String> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(
                this.springProperties.buildConsumerProperties(),
                new VoidDeserializer(),
                new StringDeserializer());
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<Void, String> kafkaListenerContainerFactory() {
        final var factory = new ConcurrentKafkaListenerContainerFactory<Void, String>();
        factory.setConsumerFactory(consumerFactory());
        factory.setReplyTemplate(kafkaTemplate());

        return factory;
    }

    @Bean
    ProducerFactory<Void, String> producerFactory() {
        return new DefaultKafkaProducerFactory<>(
                this.springProperties.buildProducerProperties(),
                new VoidSerializer(),
                new StringSerializer());
    }

    @Bean
    KafkaTemplate<Void, String> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    @Bean
    RetryTopicConfiguration retryTopicConfiguration(KafkaTemplate<Void, String> kafkaTemplate) {
        final var retry = this.applicationProperties.retry();

        return RetryTopicConfigurationBuilder.newInstance()
                .doNotAutoCreateRetryTopics()
                .suffixTopicsWithIndexValues()
                .maxAttempts(retry.attempts())
                .fixedBackOff(retry.fixedDelay())
                .dltHandlerMethod(DeadLetterTopicProcessor.ENDPOINT_HANDLER_METHOD)
                .doNotRetryOnDltFailure()
                .create(kafkaTemplate);
    }

    @Bean
    TaskScheduler scheduler() {
        return new ThreadPoolTaskScheduler();
    }

    @Override
    protected void configureCustomizers(CustomizersConfigurer customizersConfigurer) {
        customizersConfigurer.customizeErrorHandler(eh -> eh.setSeekAfterError(false));
    }
}
