package com.fatihdemir.diyetappbackend.service.Impl;

import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotRequest;
import com.fatihdemir.diyetappbackend.dto.slot.AvailableSlotResponse;
import com.fatihdemir.diyetappbackend.entity.AvailableSlot;
import com.fatihdemir.diyetappbackend.entity.Dietitian;
import com.fatihdemir.diyetappbackend.exception.*;
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
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

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
            throw new BatchSizeExceededException(MAX_BATCH_SIZE);
        }

        requests.forEach(this::validateSlotTimes);

        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

        return availableSlotRepository.findByDietitianId(dietitian.getId(), pageable)
                .map(AvailableSlotResponse::from);
    }

    public List<AvailableSlotResponse> getDietitianSlotsByDate(UUID userId, LocalDate date) {
        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

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
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

        AvailableSlot slot = availableSlotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot", slotId));

        validateSlotOwnershipAndBooking(slot, dietitian);
        availableSlotRepository.delete(slot);
    }

    @Transactional
    public void deleteAvailableSlots(UUID userId, List<UUID> slotIds) {
        if (slotIds.size() > MAX_BATCH_SIZE) {
            throw new BatchSizeExceededException(MAX_BATCH_SIZE);
        }

        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

        List<AvailableSlot> slots = availableSlotRepository.findAllById(slotIds);

        if (slots.size() != slotIds.size()) {
            throw new ResourceNotFoundException("Bir veya daha fazla slot bulunamadı");
        }

        slots.forEach(slot -> validateSlotOwnershipAndBooking(slot, dietitian));
        availableSlotRepository.deleteAll(slots);
    }

    private void validateSlotOwnershipAndBooking(AvailableSlot slot, Dietitian dietitian) {
        if (!slot.getDietitian().getId().equals(dietitian.getId())) {
            throw new AccessForbiddenException("Bu slot size ait değil");
        }
        if (slot.isBooked()) {
            throw new SlotAlreadyBookedException(slot.getId());
        }
    }

    private void validateSlotTimes(AvailableSlotRequest request) {
        if (!request.startTime().isBefore(request.endTime())) {
            throw new InvalidSlotTimeException(request.startTime(), request.endTime());
        }
    }

    private void checkOverlap(UUID dietitianId, AvailableSlotRequest request) {
        boolean overlaps = availableSlotRepository.existsByDietitianIdAndDateAndStartTimeBeforeAndEndTimeAfter(
                dietitianId, request.date(), request.endTime(), request.startTime());
        if (overlaps) {
            throw new SlotConflictException(request.startTime(), request.endTime());
        }
    }
}