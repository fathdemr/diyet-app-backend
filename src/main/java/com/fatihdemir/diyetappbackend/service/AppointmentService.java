package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentRequest;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentResponse;

import java.util.UUID;

public interface AppointmentService {

    AppointmentResponse createAppointment(UUID userId, AppointmentRequest request);

    void approveAppointment(UUID userId, UUID appointmentId);

    void rejectAppointment(UUID userId, UUID appointmentId, String reason);

    void cancelAppointment(UUID userId, UUID appointmentId, String reason);

}
