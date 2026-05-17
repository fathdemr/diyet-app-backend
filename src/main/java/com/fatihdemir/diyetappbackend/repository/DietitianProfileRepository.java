package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.DietitianProfile;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface DietitianProfileRepository extends JpaRepository<DietitianProfile, UUID> {

    Optional<DietitianProfile> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM DietitianProfile d WHERE d.id = :id")
    Optional<DietitianProfile> findByIdWithLock(@Param("id") UUID id);
}