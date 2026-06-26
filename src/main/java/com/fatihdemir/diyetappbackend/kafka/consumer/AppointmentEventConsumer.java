package com.fatihdemir.diyetappbackend.kafka.consumer;

import com.fatihdemir.diyetappbackend.kafka.event.AppointmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AppointmentEventConsumer {

    @KafkaListener(
            topics = "${kafka.topics.appointment-events}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(
            @Payload AppointmentEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("[KAFKA] Event alındı | type={} | appointmentId={} | partition={} | offset={}",
                event.eventType(), event.appointmentId(), partition, offset);

        switch (event.eventType()) {
            case CREATED -> handleCreated(event);
            case APPROVED -> handleApproved(event);
            case REJECTED -> handleRejected(event);
            case CANCELLED -> handleCancelled(event);
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.appointment-events-dlt}",
            groupId = "${spring.kafka.consumer.group-id}-dlt"
    )
    public void consumeDlt(@Payload AppointmentEvent event) {
        log.warn("[KAFKA-DLT] İşlenemeyen event kaydedildi: appointmentId={}, eventType={}",
                event.appointmentId(), event.eventType());
    }

    private void handleCreated(AppointmentEvent event) {
        log.info("[BİLDİRİM] Yeni randevu oluşturuldu: hasta={}, diyetisyen={}, tarih={} {}–{}",
                event.patientName(), event.dietitianName(),
                event.appointmentDate(), event.startTime(), event.endTime());
        // TODO: push notification / e-posta gönderimi buraya eklenecek
    }

    private void handleApproved(AppointmentEvent event) {
        log.info("[BİLDİRİM] Randevu onaylandı: hasta={}, tarih={} {}–{}",
                event.patientName(), event.appointmentDate(), event.startTime(), event.endTime());
        // TODO: hastaya "randevunuz onaylandı" bildirimi
    }

    private void handleRejected(AppointmentEvent event) {
        log.info("[BİLDİRİM] Randevu reddedildi: hasta={}, sebep={}",
                event.patientName(), event.reason());
        // TODO: hastaya "randevunuz reddedildi" bildirimi
    }

    private void handleCancelled(AppointmentEvent event) {
        log.info("[BİLDİRİM] Randevu iptal edildi: diyetisyen={}, sebep={}",
                event.dietitianName(), event.reason());
        // TODO: diyetisyene "randevu iptal edildi" bildirimi
    }
}