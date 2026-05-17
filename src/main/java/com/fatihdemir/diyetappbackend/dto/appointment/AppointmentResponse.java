package com.fatihdemir.diyetappbackend.dto.appointment;

import com.fatihdemir.diyetappbackend.entity.Appointment;
import com.fatihdemir.diyetappbackend.entity.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID clientId,
        String clientName,
        UUID dietitianId,
        String dietitianName,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        AppointmentStatus status,
        String notes,
        String cancelReason,
        LocalDateTime createdAt
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(),
                a.getClient().getId(),
                a.getClient().getFullName(),
                a.getDietitian().getId(),
                a.getDietitian().getFullName(),
                a.getStartDateTime(),
                a.getEndDateTime(),
                a.getStatus(),
                a.getNotes(),
                a.getCancelReason(),
                a.getCreatedAt()
        );
    }
}