package com.fatihdemir.diyetappbackend.kafka.config;

import com.fatihdemir.diyetappbackend.kafka.event.AppointmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

    @Value("${kafka.topics.appointment-events-dlt}")
    private String dltTopic;

    @Bean
    public DefaultErrorHandler kafkaErrorHandler(KafkaTemplate<String, AppointmentEvent> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> {
                    log.error("Kafka mesajı işlenemedi, DLT'ye gönderiliyor. topic={}, key={}, hata={}",
                            record.topic(), record.key(), ex.getMessage());
                    return new org.apache.kafka.common.TopicPartition(dltTopic, 0);
                }
        );

        // 2 kez dene, 1 saniye ara ile; sonra DLT'ye gönder
        FixedBackOff backOff = new FixedBackOff(1000L, 2L);
        return new DefaultErrorHandler(recoverer, backOff);
    }
}