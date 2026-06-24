package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentReasonRequest;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentRequest;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentResponse;
import com.fatihdemir.diyetappbackend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AppointmentController extends BaseController {

    private final AppointmentService appointmentService;

    @PostMapping("/exapi/patient/appointment")
    public ResponseEntity<AppointmentResponse> createAppointment(@RequestBody @Valid AppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.createAppointment(getCurrentUserId(), request));
    }

    @PutMapping("/exapi/dietitian/appointment/approve/{appointmentId}")
    public ResponseEntity<Void> approveAppointment(@PathVariable UUID appointmentId) {
        appointmentService.approveAppointment(getCurrentUserId(), appointmentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/exapi/dietitian/appointment/reject/{appointmentId}")
    public ResponseEntity<Void> rejectAppointment(@PathVariable UUID appointmentId,
                                                   @RequestBody @Valid AppointmentReasonRequest request) {
        appointmentService.rejectAppointment(getCurrentUserId(), appointmentId, request.reason());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/exapi/patient/appointment/cancel/{appointmentId}")
    public ResponseEntity<Void> cancelAppointment(@PathVariable UUID appointmentId,
                                                   @RequestBody @Valid AppointmentReasonRequest request) {
        appointmentService.cancelAppointment(getCurrentUserId(), appointmentId, request.reason());
        return ResponseEntity.noContent().build();
    }
}