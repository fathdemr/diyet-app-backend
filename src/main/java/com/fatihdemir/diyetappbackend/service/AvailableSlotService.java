package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotRequest;
import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface AvailableSlotService {

    AvailableSlotResponse createAvailableSlot(UUID userId, AvailableSlotRequest request);

    List<AvailableSlotResponse> createAvailableSlots(UUID userId, List<AvailableSlotRequest> requests);

    Page<AvailableSlotResponse> getDietitianSlots(UUID userId, Pageable pageable);

    List<AvailableSlotResponse> getDietitianSlotsByDate(UUID userId, LocalDate date);

    Page<AvailableSlotResponse> getAvailableSlotsForPatient(UUID dietitianId, Pageable pageable);

    List<AvailableSlotResponse> getAvailableSlotsByDateForPatient(UUID dietitianId, LocalDate date);

    void deleteAvailableSlot(UUID userId, UUID slotId);

    void deleteAvailableSlots(UUID userId, List<UUID> slotIds);
}
