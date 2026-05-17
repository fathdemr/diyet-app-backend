package com.fatihdemir.diyetappbackend.repository;

import com.fatihdemir.diyetappbackend.entity.DietitianProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DietitianProfileRepository extends JpaRepository<DietitianProfile, UUID> {
    Optional<DietitianProfile> findByUserId(UUID userId);
}