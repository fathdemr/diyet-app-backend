package com.fatihdemir.diyetappbackend.dto;

import com.fatihdemir.diyetappbackend.entity.DietitianProfile;

import java.time.LocalDateTime;
import java.util.UUID;

public record DietitianResponse(
        UUID id,
        String firstName,
        String lastName,
        String fullName,
        String bio,
        String specialization,
        Integer experienceYear,
        String city,
        LocalDateTime createdAt
) {
    public static DietitianResponse from(DietitianProfile p) {
        return new DietitianResponse(
                p.getUserId(),
                p.getFirstName(),
                p.getLastName(),
                p.getFullName(),
                p.getBio(),
                p.getSpecialization(),
                p.getExperienceYear(),
                p.getCity(),
                p.getCreatedAt()
        );
    }
}