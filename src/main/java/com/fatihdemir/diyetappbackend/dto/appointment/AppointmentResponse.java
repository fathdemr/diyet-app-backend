package com.fatihdemir.diyetappbackend.dto.appointment;

import com.fatihdemir.diyetappbackend.entity.Appointment;
import com.fatihdemir.diyetappbackend.entity.AppointmentStatus;

import java.time.LocalDate;
import java.time.LocalTime;

public record AppointmentResponse(
        AppointmentStatus status,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getStatus(),
                appointment.getSlot().getDate(),
                appointment.getSlot().getStartTime(),
                appointment.getSlot().getEndTime()
        );
    }
}
