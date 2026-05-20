package org.social.common.kafka.config;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.social.common.exceptions.BusinessException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.server.ResponseStatusException;

@Configuration
@ConditionalOnClass(KafkaOperations.class)
@EnableConfigurationProperties(KafkaCommonProperties.class)
public class KafkaErrorHandlingConfig {

    @Bean
    @ConditionalOnMissingBean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaOperations<?, ?> template) {
        return new DeadLetterPublishingRecoverer(template,
                (record, ex) -> new TopicPartition(record.topic() + ".dlt", record.partition()));
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer,
                                                 KafkaCommonProperties properties) {
        FixedBackOff backOff = new FixedBackOff(
                properties.getRetry().getBackoffMs(),
                properties.getRetry().getMaxAttempts()
        );
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
        handler.addNotRetryableExceptions(BusinessException.class);
        handler.addNotRetryableExceptions(ResponseStatusException.class);
        return handler;
    }
}
