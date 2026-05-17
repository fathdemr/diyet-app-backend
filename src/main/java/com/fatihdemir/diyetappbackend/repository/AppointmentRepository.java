package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.Appointment;
import com.fatihdemir.diyetappbackend.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    @Query("SELECT COUNT(a) FROM Appointment a " +
           "WHERE a.dietitian.id = :dietitianId " +
           "AND a.status IN :statuses " +
           "AND a.startDateTime < :endDateTime AND a.endDateTime > :startDateTime")
    long countConflicts(@Param("dietitianId") UUID dietitianId,
                        @Param("startDateTime") LocalDateTime startDateTime,
                        @Param("endDateTime") LocalDateTime endDateTime,
                        @Param("statuses") List<AppointmentStatus> statuses);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.client JOIN FETCH a.dietitian " +
                   "WHERE a.client.id = :clientId ORDER BY a.startDateTime DESC",
           countQuery = "SELECT COUNT(a) FROM Appointment a WHERE a.client.id = :clientId")
    Page<Appointment> findByClientIdWithDetails(@Param("clientId") UUID clientId, Pageable pageable);

    @Query(value = "SELECT a FROM Appointment a JOIN FETCH a.client JOIN FETCH a.dietitian " +
                   "WHERE a.dietitian.id = :dietitianId ORDER BY a.startDateTime DESC",
           countQuery = "SELECT COUNT(a) FROM Appointment a WHERE a.dietitian.id = :dietitianId")
    Page<Appointment> findByDietitianIdWithDetails(@Param("dietitianId") UUID dietitianId, Pageable pageable);

    Optional<Appointment> findByIdAndDietitianId(UUID id, UUID dietitianId);

    Optional<Appointment> findByIdAndClientId(UUID id, UUID clientId);

    // isBooked hesabı için: verilen aralıkla çakışan aktif randevular
    @Query("SELECT a FROM Appointment a WHERE a.dietitian.id = :dietitianId " +
           "AND a.status IN :statuses " +
           "AND a.startDateTime < :rangeEnd AND a.endDateTime > :rangeStart")
    List<Appointment> findActiveInRange(@Param("dietitianId") UUID dietitianId,
                                        @Param("rangeStart") LocalDateTime rangeStart,
                                        @Param("rangeEnd") LocalDateTime rangeEnd,
                                        @Param("statuses") List<AppointmentStatus> statuses);
}