package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.AvailableSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AvailableSlotRepository extends JpaRepository<AvailableSlot, UUID> {

    // existing.startTime < newSlot.endTime AND existing.endTime > newSlot.startTime
    boolean existsByDietitianIdAndDateAndStartTimeBeforeAndEndTimeAfter(
            UUID dietitianId, LocalDate date, LocalTime endTime, LocalTime startTime);

    Page<AvailableSlot> findByDietitianId(UUID dietitianId, Pageable pageable);

    List<AvailableSlot> findByDietitianIdAndDate(UUID dietitianId, LocalDate date);

    Page<AvailableSlot> findByDietitianIdAndBookedFalse(UUID dietitianId, Pageable pageable);

    List<AvailableSlot> findByDietitianIdAndDateAndBookedFalse(UUID dietitianId, LocalDate date);
}
