package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotRequest;
import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotResponse;
import com.fatihdemir.diyetappbackend.entity.AvailableSlot;
import com.fatihdemir.diyetappbackend.entity.Dietitian;
import com.fatihdemir.diyetappbackend.repository.AvailableSlotRepository;
import com.fatihdemir.diyetappbackend.repository.DietitianRepository;
import com.fatihdemir.diyetappbackend.service.AvailableSlotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AvailableSlotServiceImpl implements AvailableSlotService {

    private static final int MAX_BATCH_SIZE = 50;

    private final AvailableSlotRepository availableSlotRepository;
    private final DietitianRepository dietitianRepository;

    @Transactional
    public AvailableSlotResponse createAvailableSlot(UUID userId, AvailableSlotRequest request) {
        validateSlotTimes(request);

        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dietitian Not Found!"));

        checkOverlap(dietitian.getId(), request);

        AvailableSlot slot = AvailableSlot.builder()
                .dietitian(dietitian)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        availableSlotRepository.save(slot);
        return AvailableSlotResponse.from(slot);
    }

    @Transactional
    public List<AvailableSlotResponse> createAvailableSlots(UUID userId, List<AvailableSlotRequest> requests) {
        if (requests.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("Batch size cannot exceed " + MAX_BATCH_SIZE);
        }

        requests.forEach(this::validateSlotTimes);

        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dietitian Not Found!"));

        requests.forEach(req -> checkOverlap(dietitian.getId(), req));

        List<AvailableSlot> slots = requests.stream()
                .map(req -> AvailableSlot.builder()
                        .dietitian(dietitian)
                        .date(req.date())
                        .startTime(req.startTime())
                        .endTime(req.endTime())
                        .build())
                .toList();

        availableSlotRepository.saveAll(slots);
        return slots.stream()
                .map(AvailableSlotResponse::from)
                .toList();
    }

    public Page<AvailableSlotResponse> getDietitianSlots(UUID userId, Pageable pageable) {
        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dietitian Not Found!"));

        return availableSlotRepository.findByDietitianId(dietitian.getId(), pageable)
                .map(AvailableSlotResponse::from);
    }

    public List<AvailableSlotResponse> getDietitianSlotsByDate(UUID userId, LocalDate date) {
        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dietitian Not Found!"));

        return availableSlotRepository.findByDietitianIdAndDate(dietitian.getId(), date)
                .stream()
                .map(AvailableSlotResponse::from)
                .toList();
    }

    public Page<AvailableSlotResponse> getAvailableSlotsForPatient(UUID dietitianId, Pageable pageable) {
        return availableSlotRepository.findByDietitianIdAndBookedFalse(dietitianId, pageable)
                .map(AvailableSlotResponse::from);
    }

    public List<AvailableSlotResponse> getAvailableSlotsByDateForPatient(UUID dietitianId, LocalDate date) {
        return availableSlotRepository.findByDietitianIdAndDateAndBookedFalse(dietitianId, date)
                .stream()
                .map(AvailableSlotResponse::from)
                .toList();
    }

    @Transactional
    public void deleteAvailableSlot(UUID userId, UUID slotId) {
        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dietitian Not Found!"));

        AvailableSlot slot = availableSlotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot Not Found!"));

        validateSlotOwnershipAndBooking(slot, dietitian);
        availableSlotRepository.delete(slot);
    }

    @Transactional
    public void deleteAvailableSlots(UUID userId, List<UUID> slotIds) {
        if (slotIds.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("Batch size cannot exceed " + MAX_BATCH_SIZE);
        }

        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dietitian Not Found!"));

        List<AvailableSlot> slots = availableSlotRepository.findAllById(slotIds);

        if (slots.size() != slotIds.size()) {
            throw new IllegalArgumentException("One or more slots not found");
        }

        slots.forEach(slot -> validateSlotOwnershipAndBooking(slot, dietitian));
        availableSlotRepository.deleteAll(slots);
    }

    private void validateSlotOwnershipAndBooking(AvailableSlot slot, Dietitian dietitian) {
        if (!slot.getDietitian().getId().equals(dietitian.getId())) {
            throw new IllegalStateException("Slot does not belong to the current dietitian");
        }
        if (slot.isBooked()) {
            throw new IllegalStateException("Cannot delete a booked slot: " + slot.getId());
        }
    }

    private void validateSlotTimes(AvailableSlotRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new IllegalArgumentException(
                    "Start time must be before end time: " + request.startTime() + " - " + request.endTime());
        }
    }

    private void checkOverlap(UUID dietitianId, AvailableSlotRequest request) {
        boolean overlaps = availableSlotRepository.existsByDietitianIdAndDateAndStartTimeBeforeAndEndTimeAfter(
                dietitianId, request.date(), request.endTime(), request.startTime());
        if (overlaps) {
            throw new IllegalArgumentException(
                    "Slot overlaps with an existing slot: " + request.startTime() + " - " + request.endTime());
        }
    }
}