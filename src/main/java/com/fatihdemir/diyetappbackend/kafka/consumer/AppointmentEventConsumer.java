package com.fatihdemir.diyetappbackend.kafka.consumer;

import com.fatihdemir.diyetappbackend.entity.Notification;
import com.fatihdemir.diyetappbackend.entity.NotificationType;
import com.fatihdemir.diyetappbackend.kafka.event.AppointmentEvent;
import com.fatihdemir.diyetappbackend.service.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentEventConsumer {

    private final NotificationDispatcher dispatcher;

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
        // Hastaya onay
        dispatcher.dispatch(notification(
                NotificationType.EMAIL,
                event.patientEmail(),
                "Randevu Talebiniz Alındı",
                String.format("%s tarihinde %s–%s saatleri için randevu talebiniz alındı. Diyetisyeninizin onayı bekleniyor.",
                        event.appointmentDate(), event.startTime(), event.endTime())
        ));
        // Diyetisyene bildirim
        dispatcher.dispatch(notification(
                NotificationType.EMAIL,
                event.dietitianEmail(),
                "Yeni Randevu Talebi",
                String.format("%s adlı hasta %s tarihinde %s–%s saatleri için randevu talep etti.",
                        event.patientName(), event.appointmentDate(), event.startTime(), event.endTime())
        ));
    }

    private void handleApproved(AppointmentEvent event) {
        dispatcher.dispatch(notification(
                NotificationType.EMAIL,
                event.patientEmail(),
                "Randevunuz Onaylandı",
                String.format("%s tarihinde %s–%s saatleri arasındaki randevunuz onaylandı.",
                        event.appointmentDate(), event.startTime(), event.endTime())
        ));
    }

    private void handleRejected(AppointmentEvent event) {
        dispatcher.dispatch(notification(
                NotificationType.EMAIL,
                event.patientEmail(),
                "Randevunuz Reddedildi",
                String.format("%s tarihindeki randevu talebiniz reddedildi. Sebep: %s",
                        event.appointmentDate(), event.reason())
        ));
    }

    private void handleCancelled(AppointmentEvent event) {
        dispatcher.dispatch(notification(
                NotificationType.EMAIL,
                event.patientEmail(),
                "Randevunuz İptal Edildi",
                String.format("%s tarihindeki randevunuz iptal edildi. Sebep: %s",
                        event.appointmentDate(), event.reason())
        ));
    }

    private Notification notification(NotificationType type, String email, String title, String message) {
        Notification n = new Notification();
        n.setType(type);
        n.setEmail(email);
        n.setTitle(title);
        n.setMessage(message);
        return n;
    }
}