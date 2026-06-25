package com.fatihdemir.diyetappbackend.controller;

import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotRequest;
import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotResponse;
import com.fatihdemir.diyetappbackend.service.AvailableSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/exapi/dietitian/slot")
public class DietitianSlotController extends BaseController {

    private final AvailableSlotService availableSlotService;

    @PostMapping
    public AvailableSlotResponse createAvailableSlot(@RequestBody @Valid AvailableSlotRequest request) {
        return availableSlotService.createAvailableSlot(getCurrentUserId(), request);
    }

    @PostMapping("/batch")
    public List<AvailableSlotResponse> createAvailableSlots(@RequestBody @Valid List<AvailableSlotRequest> requests) {
        return availableSlotService.createAvailableSlots(getCurrentUserId(), requests);
    }

    @GetMapping
    public Page<AvailableSlotResponse> getMySlots(
            @PageableDefault(size = 20, sort = "date", direction = Sort.Direction.ASC) Pageable pageable) {
        return availableSlotService.getDietitianSlots(getCurrentUserId(), pageable);
    }

    @GetMapping("/date/{date}")
    public List<AvailableSlotResponse> getMySlotsByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return availableSlotService.getDietitianSlotsByDate(getCurrentUserId(), date);
    }

    @DeleteMapping("/{slotId}")
    public ResponseEntity<Void> deleteAvailableSlot(@PathVariable UUID slotId) {
        availableSlotService.deleteAvailableSlot(getCurrentUserId(), slotId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<Void> deleteAvailableSlots(@RequestBody List<UUID> slotIds) {
        availableSlotService.deleteAvailableSlots(getCurrentUserId(), slotIds);
        return ResponseEntity.noContent().build();
    }
}