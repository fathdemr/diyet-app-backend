package com.fatihdemir.diyetappbackend.kafka.event;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AppointmentEvent(
        UUID appointmentId,
        UUID patientId,
        String patientName,
        UUID dietitianId,
        String dietitianName,
        LocalDate appointmentDate,
        LocalTime startTime,
        LocalTime endTime,
        AppointmentEventType eventType,
        String reason,
        Instant occurredAt
) {
    public static AppointmentEvent of(
            UUID appointmentId,
            UUID patientId,
            String patientName,
            UUID dietitianId,
            String dietitianName,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime,
            AppointmentEventType eventType,
            String reason
    ) {
        return new AppointmentEvent(
                appointmentId, patientId, patientName,
                dietitianId, dietitianName,
                appointmentDate, startTime, endTime,
                eventType, reason,
                Instant.now()
        );
    }
}