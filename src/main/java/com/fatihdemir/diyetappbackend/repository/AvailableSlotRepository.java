package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.AvailableSlot;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AvailableSlotRepository extends JpaRepository<AvailableSlot, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AvailableSlot s WHERE s.id = :id")
    Optional<AvailableSlot> findByIdWithLock(@Param("id") UUID id);

    // existing.startTime < newSlot.endTime AND existing.endTime > newSlot.startTime
    boolean existsByDietitianIdAndDateAndStartTimeBeforeAndEndTimeAfter(
            UUID dietitianId, LocalDate date, LocalTime endTime, LocalTime startTime);

    List<AvailableSlot> findByDietitianIdAndDateIn(UUID dietitianId, Set<LocalDate> dates);

    Page<AvailableSlot> findByDietitianId(UUID dietitianId, Pageable pageable);

    List<AvailableSlot> findByDietitianIdAndDate(UUID dietitianId, LocalDate date);

    Page<AvailableSlot> findByDietitianIdAndBookedFalse(UUID dietitianId, Pageable pageable);

    List<AvailableSlot> findByDietitianIdAndDateAndBookedFalse(UUID dietitianId, LocalDate date);
}
