package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotResponse;
import com.fatihdemir.diyetappbackend.service.AvailableSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exapi/patient/slot")
public class PatientSlotController {

    private final AvailableSlotService availableSlotService;

    @GetMapping("/{dietitianId}")
    public ResponseEntity<Page<AvailableSlotResponse>> getAvailableSlots(
            @PathVariable UUID dietitianId,
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(availableSlotService.getAvailableSlotsForPatient(dietitianId, pageable));
    }

    @GetMapping("/{dietitianId}/date/{date}")
    public ResponseEntity<List<AvailableSlotResponse>> getAvailableSlotsByDate(
            @PathVariable UUID dietitianId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(availableSlotService.getAvailableSlotsByDateForPatient(dietitianId, date));
    }
}