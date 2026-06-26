package com.fatihdemir.diyetappbackend.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topics.appointment-events}")
    private String appointmentEventsTopic;

    @Value("${kafka.topics.appointment-events-dlt}")
    private String appointmentEventsDltTopic;

    @Bean
    public NewTopic appointmentEventsTopic() {
        return TopicBuilder.name(appointmentEventsTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic appointmentEventsDltTopic() {
        return TopicBuilder.name(appointmentEventsDltTopic)
                .partitions(1)
                .replicas(1)
                .build();
    }
}