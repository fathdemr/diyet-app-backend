package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentRequest;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentResponse;
import com.fatihdemir.diyetappbackend.entity.Appointment;
import com.fatihdemir.diyetappbackend.entity.AvailableSlot;
import com.fatihdemir.diyetappbackend.entity.Patient;
import com.fatihdemir.diyetappbackend.exception.AccessForbiddenException;
import com.fatihdemir.diyetappbackend.exception.ResourceNotFoundException;
import com.fatihdemir.diyetappbackend.exception.SlotAlreadyBookedException;
import com.fatihdemir.diyetappbackend.kafka.event.AppointmentEvent;
import com.fatihdemir.diyetappbackend.kafka.event.AppointmentEventType;
import com.fatihdemir.diyetappbackend.kafka.producer.AppointmentEventProducer;
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
    private final AppointmentEventProducer eventProducer;

    @Override
    @Transactional
    public AppointmentResponse createAppointment(UUID userId, AppointmentRequest request) {
        AvailableSlot slot = availableSlotRepository.findByIdWithLock(request.slotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot", request.slotId()));

        if (slot.isBooked()) {
            throw new SlotAlreadyBookedException(request.slotId());
        }

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Hasta"));

        slot.setBooked(true);

        Appointment appointment = Appointment.builder()
                .slot(slot)
                .patient(patient)
                .dietitian(slot.getDietitian())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        eventProducer.publish(AppointmentEvent.of(
                saved.getId(),
                patient.getId(),
                patient.getUser().getFullName(),
                slot.getDietitian().getId(),
                slot.getDietitian().getUser().getFullName(),
                slot.getDate(),
                slot.getStartTime(),
                slot.getEndTime(),
                AppointmentEventType.CREATED,
                null,
                appointment.getPatient().getUser().getEmail(),
                appointment.getDietitian().getUser().getEmail()
        ));

        return AppointmentResponse.from(saved);
    }

    @Override
    @Transactional
    public void approveAppointment(UUID userId, UUID appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu", appointmentId));

        if (!appointment.getDietitian().getUser().getId().equals(userId)) {
            throw new AccessForbiddenException("Bu randevuyu onaylama yetkiniz yok");
        }

        appointment.approve();

        eventProducer.publish(AppointmentEvent.of(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getFullName(),
                appointment.getDietitian().getId(),
                appointment.getDietitian().getUser().getFullName(),
                appointment.getSlot().getDate(),
                appointment.getSlot().getStartTime(),
                appointment.getSlot().getEndTime(),
                AppointmentEventType.APPROVED,
                null,
                appointment.getPatient().getUser().getEmail(),
                appointment.getDietitian().getUser().getEmail()
        ));
    }

    @Override
    @Transactional
    public void rejectAppointment(UUID userId, UUID appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu", appointmentId));

        if (!appointment.getDietitian().getUser().getId().equals(userId)) {
            throw new AccessForbiddenException("Bu randevuyu reddetme yetkiniz yok");
        }

        appointment.reject(reason);
        appointment.getSlot().setBooked(false);

        eventProducer.publish(AppointmentEvent.of(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getFullName(),
                appointment.getDietitian().getId(),
                appointment.getDietitian().getUser().getFullName(),
                appointment.getSlot().getDate(),
                appointment.getSlot().getStartTime(),
                appointment.getSlot().getEndTime(),
                AppointmentEventType.REJECTED,
                reason,
                appointment.getPatient().getUser().getEmail(),
                appointment.getDietitian().getUser().getEmail()
        ));
    }

    @Override
    @Transactional
    public void cancelAppointment(UUID userId, UUID appointmentId, String reason) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Randevu", appointmentId));

        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new AccessForbiddenException("Bu randevuyu iptal etme yetkiniz yok");
        }

        appointment.cancel(reason);
        appointment.getSlot().setBooked(false);

        eventProducer.publish(AppointmentEvent.of(
                appointment.getId(),
                appointment.getPatient().getId(),
                appointment.getPatient().getUser().getFullName(),
                appointment.getDietitian().getId(),
                appointment.getDietitian().getUser().getFullName(),
                appointment.getSlot().getDate(),
                appointment.getSlot().getStartTime(),
                appointment.getSlot().getEndTime(),
                AppointmentEventType.CANCELLED,
                reason,
                appointment.getPatient().getUser().getEmail(),
                appointment.getDietitian().getUser().getEmail()
        ));
    }
}