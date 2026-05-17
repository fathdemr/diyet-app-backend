package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentRequest;
import com.fatihdemir.diyetappbackend.dto.appointment.AppointmentResponse;
import com.fatihdemir.diyetappbackend.dto.appointment.CancelAppointmentRequest;
import com.fatihdemir.diyetappbackend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/exapi/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    @PreAuthorize("hasRole('CLIENTS')")
    @PostMapping
    public ResponseEntity<AppointmentResponse> create(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody AppointmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(appointmentService.createAppointment(userId, request));
    }

    @PreAuthorize("hasRole('CLIENTS')")
    @GetMapping("/me")
    public ResponseEntity<PageResponse<AppointmentResponse>> myAppointmentsAsClient(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 10, sort = "startDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getMyAppointmentsAsClient(userId, pageable));
    }

    @PreAuthorize("hasRole('DIETITIAN')")
    @GetMapping("/dietitian-schedule")
    public ResponseEntity<PageResponse<AppointmentResponse>> myAppointmentsAsDietitian(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 10, sort = "startDateTime", direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(appointmentService.getMyAppointmentsAsDietitian(userId, pageable));
    }

    @PreAuthorize("hasRole('DIETITIAN')")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<AppointmentResponse> confirm(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.confirmAppointment(userId, id));
    }

    @PreAuthorize("hasRole('DIETITIAN')")
    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> complete(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(appointmentService.completeAppointment(userId, id));
    }

    @PreAuthorize("hasRole('CLIENTS') or hasRole('DIETITIAN')")
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancel(
            @AuthenticationPrincipal String userId,
            @PathVariable UUID id,
            @Valid @RequestBody CancelAppointmentRequest request) {
        return ResponseEntity.ok(appointmentService.cancelAppointment(userId, id, request.reason()));
    }
}