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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailableSlotServiceImpl implements AvailableSlotService {

    private static final int MAX_DELETE_BATCH_SIZE = 50;
    private static final int DB_BATCH_INSERT_SIZE = 100;

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
        requests.forEach(this::validateSlotTimes);

        Dietitian dietitian = dietitianRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Diyetisyen"));

        // N+1 yerine tek sorguda ilgili tarihlerdeki mevcut slotları çek, memory'de kontrol et
        Set<LocalDate> dates = requests.stream().map(AvailableSlotRequest::date).collect(Collectors.toSet());
        List<AvailableSlot> existingSlots = availableSlotRepository.findByDietitianIdAndDateIn(dietitian.getId(), dates);
        requests.forEach(req -> checkOverlapInMemory(req, existingSlots));

        List<AvailableSlot> slots = requests.stream()
                .map(req -> AvailableSlot.builder()
                        .dietitian(dietitian)
                        .date(req.date())
                        .startTime(req.startTime())
                        .endTime(req.endTime())
                        .build())
                .toList();

        // Listeyi 100'erli parçalara böl, her parçayı ayrı saveAll ile kaydet
        List<AvailableSlot> saved = new ArrayList<>();
        for (int i = 0; i < slots.size(); i += DB_BATCH_INSERT_SIZE) {
            List<AvailableSlot> chunk = slots.subList(i, Math.min(i + DB_BATCH_INSERT_SIZE, slots.size()));
            saved.addAll(availableSlotRepository.saveAll(chunk));
        }

        return saved.stream()
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
        if (slotIds.size() > MAX_DELETE_BATCH_SIZE) {
            throw new BatchSizeExceededException(MAX_DELETE_BATCH_SIZE);
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

    private void checkOverlapInMemory(AvailableSlotRequest request, List<AvailableSlot> existingSlots) {
        boolean overlaps = existingSlots.stream()
                .anyMatch(existing -> existing.getDate().equals(request.date())
                        && existing.getStartTime().isBefore(request.endTime())
                        && existing.getEndTime().isAfter(request.startTime()));
        if (overlaps) {
            throw new SlotConflictException(request.startTime(), request.endTime());
        }
    }
}