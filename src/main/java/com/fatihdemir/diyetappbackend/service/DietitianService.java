package com.fatihdemir.diyetappbackend.service;

import com.fatihdemir.diyetappbackend.dto.PageResponse;
import com.fatihdemir.diyetappbackend.dto.availability.AvailabilityBulkRequest;
import com.fatihdemir.diyetappbackend.dto.availability.AvailabilityResponse;
import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianClientResponse;
import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianResponse;
import com.fatihdemir.diyetappbackend.dto.dietitian.DietitianUpdateRequest;
import com.fatihdemir.diyetappbackend.entity.*;
import com.fatihdemir.diyetappbackend.exception.AppointmentException;
import com.fatihdemir.diyetappbackend.exception.AuthException;
import com.fatihdemir.diyetappbackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DietitianService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);
    private final UniversityRepository universityRepository;
    private final TagRepository tagRepository;

    private final DietitianProfileRepository dietitianProfileRepository;
    private final DietitianAvailabilityRepository availabilityRepository;
    private final AppointmentRepository appointmentRepository;
    private final DietitianClientRepository dietitianClientRepository;

    // ── Profil ──────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<DietitianResponse> getDietitians(Pageable pageable) {
        return PageResponse.from(
                dietitianProfileRepository.findAll(pageable).map(DietitianResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public DietitianResponse getDietitianById(UUID userId) {
        return dietitianProfileRepository.findByUserId(userId)
                .map(DietitianResponse::from)
                .orElseThrow(() -> new AuthException("Diyetisyen bulunamadı", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public DietitianResponse updateProfile(String principalId, DietitianUpdateRequest request) {
        var profile = loadByUserId(principalId);

        if (request.firstName() != null) profile.setFirstName(request.firstName());
        if (request.lastName() != null) profile.setLastName(request.lastName());
        if (request.bio() != null) profile.setBio(request.bio());
        if (request.experienceYear() != null) profile.setExperienceYear(request.experienceYear());
        if (request.city() != null) profile.setCity(request.city());
        if (request.birthDay() != null) profile.setBirthDay(request.birthDay());
        if (request.university() != null) {
            University university = universityRepository.findById(request.university())
                    .orElseThrow(() -> new RuntimeException("University not found"));

            profile.setUniversity(university.getName());
        }
        if (request.tagIds() != null) {
            List<Tag> tags = tagRepository.findAllById(request.tagIds());
            profile.setTags(new HashSet<>(tags));
        }

        if (request.firstName() != null || request.lastName() != null) {
            String first = profile.getFirstName() != null ? profile.getFirstName() : "";
            String last = profile.getLastName() != null ? profile.getLastName() : "";
            profile.setFullName((first + " " + last).trim());
        }

        return DietitianResponse.from(dietitianProfileRepository.save(profile));
    }

    // ── Danışan Listesi ──────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public PageResponse<DietitianClientResponse> getDietitianClients(String principalUserId, Pageable pageable) {
        var dietitian = loadByUserId(principalUserId);
        return PageResponse.from(
                dietitianClientRepository.findByDietitianIdWithClient(dietitian.getId(), pageable)
                        .map(DietitianClientResponse::from)
        );
    }

    // ── Müsaitlik ────────────────────────────────────────────────────────────

    @Transactional
    public List<AvailabilityResponse> bulkCreateAvailability(String principalUserId,
                                                             AvailabilityBulkRequest request) {
        var dietitian = loadByUserId(principalUserId);

        var slots = request.slots().stream()
                .sorted(Comparator.comparing(s -> s.startDateTime()))
                .toList();

        for (var slot : slots) {
            if (!slot.startDateTime().isBefore(slot.endDateTime())) {
                throw new AppointmentException(
                        "Bitiş zamanı başlangıçtan sonra olmalı: " + slot.startDateTime(),
                        HttpStatus.BAD_REQUEST);
            }
        }

        // İstek içi çakışma kontrolü (sıralı → sadece komşuları karşılaştır)
        for (int i = 0; i < slots.size() - 1; i++) {
            var a = slots.get(i);
            var b = slots.get(i + 1);
            if (a.endDateTime().isAfter(b.startDateTime())) {
                throw new AppointmentException(
                        "İstekteki slotlar çakışıyor: " + a.startDateTime() + " ↔ " + b.startDateTime(),
                        HttpStatus.BAD_REQUEST);
            }
        }

        // DB'deki mevcut slotlarla çakışma kontrolü (tek sorgu, tüm batch için)
        var rangeStart = slots.getFirst().startDateTime();
        var rangeEnd = slots.getLast().endDateTime();
        var existing = availabilityRepository.findOverlapping(dietitian.getId(), rangeStart, rangeEnd);

        if (!existing.isEmpty()) {
            var c = existing.getFirst();
            throw new AppointmentException(
                    "Mevcut müsaitlik kaydıyla çakışıyor: " + c.getStartDateTime() + " - " + c.getEndDateTime(),
                    HttpStatus.CONFLICT);
        }

        var saved = availabilityRepository.saveAll(
                slots.stream().map(slot -> {
                    var av = new DietitianAvailability();
                    av.setDietitian(dietitian);
                    av.setStartDateTime(slot.startDateTime());
                    av.setEndDateTime(slot.endDateTime());
                    return av;
                }).toList()
        );

        return saved.stream().map(a -> AvailabilityResponse.from(a, false)).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<AvailabilityResponse> getAvailability(UUID dietitianId, Pageable pageable) {
        Page<DietitianAvailability> page = availabilityRepository.findByDietitianId(dietitianId, pageable);

        if (page.isEmpty()) {
            return PageResponse.from(page.map(a -> AvailabilityResponse.from(a, false)));
        }

        // Sayfa aralığındaki aktif randevuları tek sorguda çek, isBooked'u Java'da hesapla
        LocalDateTime rangeStart = page.getContent().getFirst().getStartDateTime();
        LocalDateTime rangeEnd = page.getContent().getLast().getEndDateTime();

        List<Appointment> active = appointmentRepository.findActiveInRange(
                dietitianId, rangeStart, rangeEnd, ACTIVE_STATUSES);

        return PageResponse.from(page.map(slot -> {
            boolean booked = active.stream().anyMatch(apt ->
                    apt.getStartDateTime().isBefore(slot.getEndDateTime()) &&
                            apt.getEndDateTime().isAfter(slot.getStartDateTime()));
            return AvailabilityResponse.from(slot, booked);
        }));
    }

    @Transactional
    public void deleteAvailabilitySlot(String principalUserId, UUID slotId) {
        var dietitian = loadByUserId(principalUserId);

        var slot = availabilityRepository.findByIdAndDietitianId(slotId, dietitian.getId())
                .orElseThrow(() -> new AppointmentException("Müsaitlik kaydı bulunamadı", HttpStatus.NOT_FOUND));

        long active = appointmentRepository.countConflicts(
                dietitian.getId(), slot.getStartDateTime(), slot.getEndDateTime(), ACTIVE_STATUSES);

        if (active > 0) {
            throw new AppointmentException(
                    "Bu zaman diliminde aktif randevu bulunduğu için silinemez", HttpStatus.CONFLICT);
        }

        availabilityRepository.delete(slot);
    }

    // ── Yardımcı ─────────────────────────────────────────────────────────────

    private DietitianProfile loadByUserId(String principalUserId) {
        return dietitianProfileRepository.findByUserId(UUID.fromString(principalUserId))
                .orElseThrow(() -> new AuthException("Diyetisyen bulunamadı", HttpStatus.NOT_FOUND));
    }
}