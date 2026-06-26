package com.fatihdemir.diyetappbackend.kafka.producer;

import com.fatihdemir.diyetappbackend.kafka.event.AppointmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentEventProducer {

    private final KafkaTemplate<String, AppointmentEvent> kafkaTemplate;

    @Value("${kafka.topics.appointment-events}")
    private String topic;

    public void publish(AppointmentEvent event) {
        String key = event.appointmentId().toString();

        CompletableFuture<SendResult<String, AppointmentEvent>> future =
                kafkaTemplate.send(topic, key, event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Randevu eventi gönderilemedi: appointmentId={}, eventType={}, hata={}",
                        event.appointmentId(), event.eventType(), ex.getMessage());
            } else {
                log.info("Randevu eventi gönderildi: appointmentId={}, eventType={}, partition={}, offset={}",
                        event.appointmentId(), event.eventType(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }
}