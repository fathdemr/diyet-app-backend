package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.DietitianAvailability;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DietitianAvailabilityRepository extends JpaRepository<DietitianAvailability, UUID> {

    // Verilen [start, end) aralığıyla çakışan mevcut slotlar
    @Query("SELECT a FROM DietitianAvailability a WHERE a.dietitian.id = :dietitianId " +
           "AND a.startDateTime < :endDateTime AND a.endDateTime > :startDateTime")
    List<DietitianAvailability> findOverlapping(@Param("dietitianId") UUID dietitianId,
                                                @Param("startDateTime") LocalDateTime startDateTime,
                                                @Param("endDateTime") LocalDateTime endDateTime);

    // İstenen slot en az bir availability bloğu içinde mi?
    @Query("SELECT COUNT(a) FROM DietitianAvailability a WHERE a.dietitian.id = :dietitianId " +
           "AND a.startDateTime <= :startDateTime AND a.endDateTime >= :endDateTime")
    long countContaining(@Param("dietitianId") UUID dietitianId,
                         @Param("startDateTime") LocalDateTime startDateTime,
                         @Param("endDateTime") LocalDateTime endDateTime);

    // isBooked hesabı için belirli aralıktaki tüm slotlar
    @Query("SELECT a FROM DietitianAvailability a WHERE a.dietitian.id = :dietitianId " +
           "AND a.startDateTime < :rangeEnd AND a.endDateTime > :rangeStart " +
           "ORDER BY a.startDateTime ASC")
    List<DietitianAvailability> findInRange(@Param("dietitianId") UUID dietitianId,
                                            @Param("rangeStart") LocalDateTime rangeStart,
                                            @Param("rangeEnd") LocalDateTime rangeEnd);

    @Query(value = "SELECT a FROM DietitianAvailability a WHERE a.dietitian.id = :dietitianId " +
                   "ORDER BY a.startDateTime ASC",
           countQuery = "SELECT COUNT(a) FROM DietitianAvailability a WHERE a.dietitian.id = :dietitianId")
    Page<DietitianAvailability> findByDietitianId(@Param("dietitianId") UUID dietitianId, Pageable pageable);

    Optional<DietitianAvailability> findByIdAndDietitianId(UUID id, UUID dietitianId);
}