package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentRequest;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentResponse;
import com.fatihdemir.diyetappbackend.entity.Appointment;
import com.fatihdemir.diyetappbackend.entity.AvailableSlot;
import com.fatihdemir.diyetappbackend.entity.Patient;
import com.fatihdemir.diyetappbackend.repository.AppointmentRepository;
import com.fatihdemir.diyetappbackend.repository.AvailableSlotRepository;
import com.fatihdemir.diyetappbackend.repository.PatientRepository;
import com.fatihdemir.diyetappbackend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final AvailableSlotRepository availableSlotRepository;
    private final PatientRepository patientRepository;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(UUID userId, AppointmentRequest request) {
        AvailableSlot slot = availableSlotRepository.findByIdWithLock(request.slotId())
                .orElseThrow(() -> new IllegalArgumentException("Available slot not found"));

        if (slot.isBooked()) {
            throw new IllegalStateException("Slot is already booked");
        }

        Patient patient = patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found"));

        slot.setBooked(true);

        Appointment appointment = Appointment.builder()
                .slot(slot)
                .patient(patient)
                .dietitian(slot.getDietitian())
                .build();

        return AppointmentResponse.from(appointmentRepository.save(appointment));
    }

    @Override
    @Transactional
    public void approveAppointment(UUID userId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getDietitian().getUser().getId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to approve this appointment");
        }

        appointment.approve();
    }

    @Override
    @Transactional
    public void rejectAppointment(UUID userId, UUID appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getDietitian().getUser().getId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to reject this appointment");
        }

        appointment.reject(reason);
        appointment.getSlot().setBooked(false);
    }

    @Override
    @Transactional
    public void cancelAppointment(UUID userId, UUID appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found"));

        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new IllegalStateException("You are not authorized to cancel this appointment");
        }

        appointment.cancel(reason);
        appointment.getSlot().setBooked(false);
    }

}